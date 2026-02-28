package com.bff.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores the OAuth2AuthorizedClient (access-token + refresh-token) in an
 * AES-256-GCM encrypted HttpOnly cookie — no server-side session required.
 *
 * Implements the Servlet-stack OAuth2AuthorizedClientRepository — plain Java,
 * no Mono, no WebFlux.
 *
 * Security model:
 *   • The cookie is HttpOnly     → JS cannot exfiltrate the tokens (XSS-safe)
 *   • The cookie is Secure       → sent only over HTTPS
 *   • The value is AES-256-GCM   → confidential + tamper-evident
 *   • Client registration meta   → always loaded from the server-side repository,
 *                                   never trusted from the cookie payload
 */
@Slf4j
@Component
public class CookieOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private final TokenCookieManager          tokenCookieManager;
    private final ClientRegistrationRepository clientRegistrations;
    private final ObjectMapper                objectMapper;

    public CookieOAuth2AuthorizedClientRepository(
            TokenCookieManager tokenCookieManager,
            ClientRegistrationRepository clientRegistrations,
            ObjectMapper objectMapper) {
        this.tokenCookieManager  = tokenCookieManager;
        this.clientRegistrations = clientRegistrations;
        this.objectMapper        = objectMapper;
    }

    // ── OAuth2AuthorizedClientRepository contract ─────────────────────────────

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
            String clientRegistrationId,
            Authentication principal,
            HttpServletRequest request) {

        return tokenCookieManager.readTokenCookie(request)
                .map(json -> {
                    try {
                        Map<String, Object> payload = objectMapper.readValue(json, Map.class);
                        String storedId = (String) payload.get("clientRegistrationId");

                        if (!clientRegistrationId.equals(storedId)) return null;

                        return (T) buildAuthorizedClient(payload);
                    } catch (Exception e) {
                        log.warn("Failed to deserialize authorized client from cookie: {}",
                                e.getMessage());
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient,
                                      Authentication principal,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        try {
            Map<String, Object> payload = buildPayload(authorizedClient);
            String json = objectMapper.writeValueAsString(payload);
            tokenCookieManager.writeTokenCookie(json, response);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize authorized client to cookie", e);
        }
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId,
                                        Authentication principal,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        tokenCookieManager.clearTokenCookie(response);
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    /**
     * Serializes only what is needed to reconstruct the client later:
     * access-token, refresh-token, and registration ID.
     * The id-token is intentionally excluded (it is only needed at login).
     */
    private Map<String, Object> buildPayload(OAuth2AuthorizedClient client) {
        Map<String, Object> map = new HashMap<>();

        map.put("clientRegistrationId", client.getClientRegistration().getRegistrationId());
        map.put("principalName",        client.getPrincipalName());

        OAuth2AccessToken at = client.getAccessToken();
        map.put("accessToken",          at.getTokenValue());
        map.put("accessTokenType",      at.getTokenType().getValue());
        map.put("accessTokenIssuedAt",  at.getIssuedAt()  != null ? at.getIssuedAt().toString()  : null);
        map.put("accessTokenExpiresAt", at.getExpiresAt() != null ? at.getExpiresAt().toString() : null);
        map.put("accessTokenScopes",    at.getScopes());

        if (client.getRefreshToken() != null) {
            OAuth2RefreshToken rt = client.getRefreshToken();
            map.put("refreshToken",         rt.getTokenValue());
            map.put("refreshTokenIssuedAt", rt.getIssuedAt() != null ? rt.getIssuedAt().toString() : null);
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    private OAuth2AuthorizedClient buildAuthorizedClient(Map<String, Object> payload) {
        String registrationId = (String) payload.get("clientRegistrationId");
        String principalName  = (String) payload.get("principalName");

        // Always load registration from the server-side repository — never trust the cookie
        var registration = clientRegistrations.findByRegistrationId(registrationId);
        if (registration == null) {
            log.warn("Client registration '{}' not found", registrationId);
            return null;
        }

        // Reconstruct access token
        Set<String> scopes = new HashSet<>();
        Object rawScopes = payload.get("accessTokenScopes");
        if (rawScopes instanceof List<?> list) {
            list.forEach(s -> scopes.add(s.toString()));
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                (String) payload.get("accessToken"),
                parseInstant((String) payload.get("accessTokenIssuedAt")),
                parseInstant((String) payload.get("accessTokenExpiresAt")),
                scopes
        );

        // Reconstruct refresh token (optional)
        OAuth2RefreshToken refreshToken = null;
        if (payload.containsKey("refreshToken")) {
            refreshToken = new OAuth2RefreshToken(
                    (String) payload.get("refreshToken"),
                    parseInstant((String) payload.get("refreshTokenIssuedAt"))
            );
        }

        return new OAuth2AuthorizedClient(registration, principalName, accessToken, refreshToken);
    }

    private Instant parseInstant(String value) {
        return value != null ? Instant.parse(value) : null;
    }
}
