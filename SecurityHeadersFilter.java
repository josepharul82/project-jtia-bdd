package com.bff.filter;

import com.bff.properties.BffProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds security response headers not covered by Spring Security's built-in
 * header writers, and strips headers that reveal implementation details.
 *
 * Applied to every response via OncePerRequestFilter (plain Servlet, no Mono).
 */
@Component
@Order(-99)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private final BffProperties props;

    public SecurityHeadersFilter(BffProperties props) {
        this.props = props;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        // Run the rest of the filter chain first so that downstream code
        // can set its own headers, then we clean up afterwards.
        chain.doFilter(request, response);

        // ── Headers to add ────────────────────────────────────────────────────
        response.setHeader("Permissions-Policy", props.headers().permissionsPolicy());
        response.setHeader("Cache-Control",      "no-store");
        response.setHeader("Pragma",             "no-cache");

        // ── Headers to remove (information disclosure) ────────────────────────
        response.setHeader("Server",       null);
        response.setHeader("X-Powered-By", null);
    }
}
