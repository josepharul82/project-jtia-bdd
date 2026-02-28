package com.bff.filter;

import com.bff.properties.BffProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Adds the SameSite attribute to all Set-Cookie response headers.
 *
 * The Servlet API (before Jakarta Servlet 6) has no native SameSite setter
 * on javax/jakarta.servlet.http.Cookie. The standard workaround is to post-process
 * the Set-Cookie response headers in a filter.
 *
 * Rules applied:
 *   BFF_TOKEN, BFF_SAVED_REQUEST → SameSite=Lax
 *     (must be sent on the cross-site redirect back from the IdP)
 *
 *   XSRF-TOKEN                  → SameSite=Strict
 *     (never needs to be sent cross-site; React reads + injects it as a header)
 *
 *   All others                  → SameSite=Lax  (safe default)
 */
@Slf4j
@Component
@Order(-100)
public class SameSiteCookieFilter extends OncePerRequestFilter {

    private final BffProperties props;

    public SameSiteCookieFilter(BffProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        chain.doFilter(request, new SameSiteResponseWrapper(response));
    }

    // ── Response wrapper ──────────────────────────────────────────────────────

    private class SameSiteResponseWrapper extends HttpServletResponseWrapper {

        SameSiteResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setHeader(String name, String value) {
            super.setHeader(name, applySameSite(name, value));
        }

        @Override
        public void addHeader(String name, String value) {
            super.addHeader(name, applySameSite(name, value));
        }

        @Override
        public Collection<String> getHeaders(String name) {
            Collection<String> headers = super.getHeaders(name);
            if (!"Set-Cookie".equalsIgnoreCase(name)) return headers;

            List<String> result = new ArrayList<>();
            headers.forEach(h -> result.add(applySameSite(name, h)));
            return result;
        }

        private String applySameSite(String headerName, String headerValue) {
            if (!"Set-Cookie".equalsIgnoreCase(headerName)) return headerValue;
            if (headerValue == null || headerValue.isBlank()) return headerValue;

            // Do not add SameSite if it is already present
            if (headerValue.toLowerCase().contains("samesite")) return headerValue;

            String sameSite = resolveSameSite(headerValue);
            return headerValue + "; SameSite=" + sameSite;
        }

        private String resolveSameSite(String cookieHeader) {
            String lower = cookieHeader.toLowerCase();

            // CSRF token: Strict – React reads + echoes as a header, never sent automatically
            if (lower.startsWith(props.cookie().csrfCookieName().toLowerCase())) {
                return "Strict";
            }

            // All BFF cookies (token + saved-request): Lax
            // Lax is required so they are sent when the IdP redirects back to the BFF.
            return "Lax";
        }
    }
}
