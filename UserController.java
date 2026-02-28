package com.bff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * BFF user-info endpoint.
 *
 * GET /bff/user
 *   Authenticated   → 200 + safe subset of OIDC claims
 *   Unauthenticated → 401 (handled by SecurityConfig entry point before reaching here)
 *
 * Plain Spring MVC — no Mono, no reactive types.
 *
 * Security notes:
 *   • Only a safe, curated subset of claims is returned
 *   • The access-token is NEVER returned to the SPA
 *   • No caching so session state is always fresh
 */
@RestController
@RequestMapping("/bff")
public class UserController {

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> currentUser(
            @AuthenticationPrincipal OidcUser oidcUser) {

        if (oidcUser == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("sub",         oidcUser.getSubject());
        userInfo.put("name",        safe(oidcUser.getFullName()));
        userInfo.put("given_name",  safe(oidcUser.getGivenName()));
        userInfo.put("family_name", safe(oidcUser.getFamilyName()));
        userInfo.put("email",       safe(oidcUser.getEmail()));

        return ResponseEntity.ok(userInfo);
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}
