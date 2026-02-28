package com.bff.oauth2;

import com.bff.properties.BffProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Stores the pre-login target URL in an encrypted HttpOnly cookie.
 *
 * Replaces the default HttpSessionRequestCache — no session needed.
 * Implements the Servlet-stack RequestCache — plain Java, no Mono, no WebFlux.
 *
 * ─── Why this is needed ───────────────────────────────────────────────────────
 * When an unauthenticated user hits a protected URL (e.g. /api/v1/projets),
 * Spring Security must:
 *   1. Save the original URL so it can redirect back after login
 *   2. Redirect the user to the IdP login page
 *   3. After successful login, redirect back to the saved URL
 *
 * The default implementation saves this URL in the HttpSession. Since our BFF
 * is stateless (SessionCreationPolicy.STATELESS), we store it in a short-lived
 * encrypted HttpOnly cookie instead.
 *
 * ─── Security ─────────────────────────────────────────────────────────────────
 *   • The URL is AES-256-GCM encrypted  → cannot be read or tampered with
 *   • The cookie is HttpOnly             → JS cannot modify the redirect target
 *   • Max-Age is 5 minutes              → automatically expires after the login flow
 *   • Open-redirect protection          → only same-origin paths are honoured;
 *                                         absolute URLs with a different host are
 *                                         rejected and replaced with "/"
 */
@Slf4j
@Component
public class CookieSavedRequestRepository implements RequestCache {

    private final BffProperties      props;
    private final TokenCookieManager tokenCookieManager;

    public CookieSavedRequestRepository(BffProperties props,
                                         TokenCookieManager tokenCookieManager) {
        this.props              = props;
        this.tokenCookieManager = tokenCookieManager;
    }

    // ── RequestCache contract ─────────────────────────────────────────────────

    /**
     * Called by Spring Security before redirecting to the IdP.
     * Saves the original request URL in a short-lived encrypted cookie.
     */
    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        String requestUrl = buildRequestUrl(request);

        if (!isSafeToSave(requestUrl)) {
            log.debug("Skipping save of internal/unsafe URL: {}", requestUrl);
            return;
        }

        tokenCookieManager.writeSavedRequestCookie(requestUrl, response);
        log.debug("Saved pre-login request URL to cookie: {}", requestUrl);
    }

    /**
     * Called by Spring Security after a successful login to find the redirect target.
     * Reads and decrypts the saved URL from the cookie.
     */
    @Override
    public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
        return tokenCookieManager.readSavedRequestCookie(request)
                .map(url -> validateSameOrigin(url, request))
                .map(SimpleSavedRequest::new)
                .orElse(null);
    }

    /**
     * Called after the saved request has been consumed (user redirected back).
     * Returns the current request and clears the cookie.
     */
    @Override
    public HttpServletRequest getMatchingRequest(HttpServletRequest request,
                                                  HttpServletResponse response) {
        SavedRequest saved = getRequest(request, response);
        if (saved == null) return null;

        // Clear the cookie — it has been consumed
        tokenCookieManager.clearSavedRequestCookie(response);
        return request;
    }

    /**
     * Explicitly removes the saved request cookie (e.g. when the user cancels login).
     */
    @Override
    public void removeRequest(HttpServletRequest request, HttpServletResponse response) {
        tokenCookieManager.clearSavedRequestCookie(response);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds the full request URL including the query string.
     * Stores only the path + query (relative), not the scheme + host,
     * so the URL stays valid across HTTP/HTTPS transitions in dev.
     */
    private String buildRequestUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getRequestURI());
        String query = request.getQueryString();
        if (query != null && !query.isBlank()) {
            url.append('?').append(query);
        }
        return url.toString();
    }

    /**
     * Only saves paths that make sense to return to after login.
     * Never saves OAuth2/login handshake paths, the BFF's own endpoints,
     * or the actuator — these would cause redirect loops or expose internals.
     */
    private boolean isSafeToSave(String url) {
        return !url.startsWith("/oauth2/")
            && !url.startsWith("/login")
            && !url.startsWith("/logout")
            && !url.startsWith("/bff/")
            && !url.startsWith("/actuator/");
    }

    /**
     * Open-redirect protection.
     *
     * If the stored URL is absolute, verifies it targets the same host and port
     * as the current request. If not (e.g. an attacker injected an external URL),
     * returns "/" as a safe fallback.
     *
     * Relative paths (the normal case) are always safe and returned as-is.
     */
    private String validateSameOrigin(String url, HttpServletRequest request) {
        try {
            URI uri = new URI(url);

            if (!uri.isAbsolute()) {
                return url; // relative path — always safe
            }

            String requestHost = request.getServerName();
            int    requestPort = request.getServerPort();

            if (!uri.getHost().equals(requestHost) || uri.getPort() != requestPort) {
                log.warn("Blocked open-redirect attempt to external URL: {}", url);
                return "/";
            }

            // Absolute URL on the same origin — strip scheme+host, keep path+query
            return uri.getRawPath()
                    + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");

        } catch (URISyntaxException e) {
            log.warn("Malformed saved-request URL, falling back to /: {}", url);
            return "/";
        }
    }
}
