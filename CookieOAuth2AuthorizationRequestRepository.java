package com.bff.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * Stores the OAuth2AuthorizationRequest (state + PKCE code verifier) in an
 * encrypted HttpOnly cookie instead of the HTTP session.
 *
 * Implements the Servlet-stack AuthorizationRequestRepository — plain Java,
 * no Mono, no WebFlux.
 *
 * The cookie is short-lived (5 minutes) and only exists during the login flow:
 *   1. User hits protected resource → BFF saves state cookie → redirect to IdP
 *   2. IdP redirects back → BFF reads + deletes state cookie → verifies state
 */
@Slf4j
@Component
public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final TokenCookieManager tokenCookieManager;
    private final ObjectMapper       objectMapper;

    public CookieOAuth2AuthorizationRequestRepository(TokenCookieManager tokenCookieManager,
                                                       ObjectMapper objectMapper) {
        this.tokenCookieManager = tokenCookieManager;
        this.objectMapper       = objectMapper;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return tokenCookieManager.readSavedRequestCookie(request)
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, OAuth2AuthorizationRequest.class);
                    } catch (Exception e) {
                        log.debug("Could not deserialize OAuth2AuthorizationRequest from cookie", e);
                        return null;
                    }
                })
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        if (authorizationRequest == null) {
            tokenCookieManager.clearSavedRequestCookie(response);
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(authorizationRequest);
            tokenCookieManager.writeSavedRequestCookie(json, response);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize OAuth2AuthorizationRequest", e);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        // Clear the cookie whether we found a request or not
        tokenCookieManager.clearSavedRequestCookie(response);
        return authRequest;
    }
}
