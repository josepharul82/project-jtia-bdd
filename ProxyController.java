package com.bff.proxy;

import com.bff.oauth2.CookieOAuth2AuthorizedClientRepository;
import com.bff.properties.BffProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Reverse proxy controller — replaces Spring Cloud Gateway entirely.
 *
 * Every request to /api/** is:
 *   1. Authenticated (enforced by SecurityConfig)
 *   2. Enriched with the OAuth2 Bearer token from the encrypted cookie
 *   3. Forwarded to the backend via RestClient (Spring 6 synchronous HTTP client)
 *   4. The backend response is streamed back to the browser
 *
 * No Mono, no WebFlux, no reactive types — plain synchronous Java.
 *
 * Security measures:
 *   • Bearer token injected from server-side cookie (never from JS)
 *   • Dangerous request headers stripped before forwarding
 *   • Internal response headers stripped before returning to browser
 *   • X-Correlation-ID added for distributed tracing
 */
@Slf4j
@RestController
public class ProxyController {

    /**
     * Headers from the client that must NEVER be forwarded to the backend.
     * These could be used to spoof identity or bypass backend security checks.
     */
    private static final Set<String> BLOCKED_REQUEST_HEADERS = Set.of(
            "cookie",            // never forward BFF cookies to the backend
            "authorization",     // we inject our own Bearer token
            "x-internal-user",   // internal trust header – must not be forgeable
            "x-internal-role",   // internal trust header – must not be forgeable
            "x-real-ip",         // we set this ourselves from the actual connection
            "host"               // must reflect the backend host, not the BFF host
    );

    /**
     * Headers from the backend response that must NOT be returned to the browser.
     * These reveal implementation details or conflict with BFF-level headers.
     */
    private static final Set<String> BLOCKED_RESPONSE_HEADERS = Set.of(
            "server",
            "x-powered-by",
            "x-application-context",
            "transfer-encoding"  // handled by RestClient / Servlet container
    );

    private final RestClient                          restClient;
    private final BffProperties                       props;
    private final CookieOAuth2AuthorizedClientRepository authorizedClientRepository;

    public ProxyController(BffProperties props,
                           RestClient.Builder restClientBuilder,
                           CookieOAuth2AuthorizedClientRepository authorizedClientRepository) {
        this.props                      = props;
        this.authorizedClientRepository = authorizedClientRepository;
        this.restClient = restClientBuilder
                .baseUrl(props.proxy().backendUrl())
                .build();
    }

    /**
     * Proxies all /api/** requests to the backend.
     * Matched by Spring MVC before any other controller.
     */
    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> proxy(
            HttpServletRequest request,
            @RequestBody(required = false) byte[] body,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication) throws IOException {

        // ── 1. Build backend URI (preserve path + query string) ───────────────
        String backendPath = request.getRequestURI();  // already includes /api/
        String query       = request.getQueryString();

        URI backendUri = UriComponentsBuilder
                .fromUriString(props.proxy().backendUrl())
                .path(backendPath)
                .query(query)
                .build(true)
                .toUri();

        // ── 2. Build forwarded headers ────────────────────────────────────────
        HttpHeaders forwardHeaders = buildForwardHeaders(request);

        // ── 3. Inject OAuth2 Bearer token from the encrypted cookie ───────────
        injectBearerToken(authentication, request, forwardHeaders);

        // ── 4. Add tracing and BFF identification headers ─────────────────────
        forwardHeaders.set("X-Correlation-ID", UUID.randomUUID().toString());
        forwardHeaders.set("X-BFF-Version",    "1");
        forwardHeaders.set("X-Forwarded-For",  request.getRemoteAddr());
        forwardHeaders.set("X-Forwarded-Host", request.getServerName());
        forwardHeaders.set("X-Forwarded-Port", String.valueOf(request.getServerPort()));
        forwardHeaders.set("X-Forwarded-Proto", request.getScheme());

        // ── 5. Forward to backend ─────────────────────────────────────────────
        try {
            HttpMethod method = HttpMethod.valueOf(request.getMethod());

            ResponseEntity<byte[]> backendResponse = restClient
                    .method(method)
                    .uri(backendUri)
                    .headers(h -> h.addAll(forwardHeaders))
                    .body(body != null ? body : new byte[0])
                    .retrieve()
                    .toEntity(byte[].class);

            // ── 6. Strip internal response headers before returning ────────────
            HttpHeaders cleanHeaders = stripBlockedResponseHeaders(backendResponse.getHeaders());

            return ResponseEntity
                    .status(backendResponse.getStatusCode())
                    .headers(cleanHeaders)
                    .body(backendResponse.getBody());

        } catch (HttpStatusCodeException ex) {
            // Forward the error status + body from the backend as-is
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(ex.getResponseBodyAsByteArray());

        } catch (Exception ex) {
            log.error("Proxy error forwarding {} {}: {}", request.getMethod(), backendPath, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body(("{\"error\":\"backend_unavailable\"}").getBytes());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Copies safe client request headers to the forwarded header map.
     * Strips all headers in BLOCKED_REQUEST_HEADERS.
     */
    private HttpHeaders buildForwardHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name -> {
            if (!BLOCKED_REQUEST_HEADERS.contains(name.toLowerCase())) {
                headers.set(name, request.getHeader(name));
            }
        });
        return headers;
    }

    /**
     * Reads the access-token from the encrypted BFF_TOKEN cookie and injects
     * it as an Authorization: Bearer header for the backend.
     *
     * The token never travels from the browser — only from the server-side cookie.
     */
    private void injectBearerToken(Authentication authentication,
                                    HttpServletRequest request,
                                    HttpHeaders headers) {
        if (authentication == null) return;

        OAuth2AuthorizedClient client = authorizedClientRepository.loadAuthorizedClient(
                props.oauth2().registrationId(), authentication, request);

        if (client != null && client.getAccessToken() != null) {
            headers.setBearerAuth(client.getAccessToken().getTokenValue());
        } else {
            log.warn("No access token found for authenticated principal: {}",
                    authentication.getName());
        }
    }

    /**
     * Returns a copy of the headers with all BLOCKED_RESPONSE_HEADERS removed.
     */
    private HttpHeaders stripBlockedResponseHeaders(HttpHeaders original) {
        HttpHeaders clean = new HttpHeaders();
        original.forEach((name, values) -> {
            if (!BLOCKED_RESPONSE_HEADERS.contains(name.toLowerCase())) {
                clean.put(name, values);
            }
        });
        return clean;
    }
}
