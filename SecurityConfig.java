package com.bff.config;

import com.bff.oauth2.CookieOAuth2AuthorizationRequestRepository;
import com.bff.oauth2.CookieOAuth2AuthorizedClientRepository;
import com.bff.oauth2.CookieSavedRequestRepository;
import com.bff.properties.BffProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 *  BFF Security Configuration – Spring MVC (Servlet stack)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *  Zero reactive code. Uses standard HttpSecurity, HttpServletRequest,
 *  HttpServletResponse throughout.
 *
 *  STATELESS DESIGN
 *  SessionCreationPolicy.STATELESS ensures Spring Security never creates or
 *  reads an HttpSession. All state is carried in encrypted HttpOnly cookies.
 *
 *  COOKIE MAP
 *  ┌──────────────────────┬──────────┬────────┬──────────┬─────────────────────┐
 *  │ Cookie               │ HttpOnly │ Secure │ SameSite │ Purpose             │
 *  ├──────────────────────┼──────────┼────────┼──────────┼─────────────────────┤
 *  │ BFF_TOKEN            │ YES      │ YES*   │ Lax      │ Encrypted tokens    │
 *  ├──────────────────────┼──────────┼────────┼──────────┼─────────────────────┤
 *  │ BFF_SAVED_REQUEST    │ YES      │ YES*   │ Lax      │ Pre-login URL 5 min │
 *  ├──────────────────────┼──────────┼────────┼──────────┼─────────────────────┤
 *  │ XSRF-TOKEN           │ NO       │ YES*   │ Strict   │ CSRF double-submit  │
 *  └──────────────────────┴──────────┴────────┴──────────┴─────────────────────┘
 *  * Secure=false in dev profile only
 *
 *  AUTH FLOW
 *  Browser nav  → 302 /oauth2/authorization/{provider} → IdP → 302 back to saved URL
 *  AJAX /api/** → 401 JSON { "error": "unauthenticated", "loginUrl": "..." }
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final BffProperties                              props;
    private final ClientRegistrationRepository               clientRegistrations;
    private final CookieOAuth2AuthorizationRequestRepository authRequestRepository;
    private final CookieOAuth2AuthorizedClientRepository     authorizedClientRepository;
    private final CookieSavedRequestRepository               savedRequestRepository;

    public SecurityConfig(
            BffProperties props,
            ClientRegistrationRepository clientRegistrations,
            CookieOAuth2AuthorizationRequestRepository authRequestRepository,
            CookieOAuth2AuthorizedClientRepository authorizedClientRepository,
            CookieSavedRequestRepository savedRequestRepository) {
        this.props                      = props;
        this.clientRegistrations        = clientRegistrations;
        this.authRequestRepository      = authRequestRepository;
        this.authorizedClientRepository = authorizedClientRepository;
        this.savedRequestRepository     = savedRequestRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ── STATELESS – never create or use an HttpSession ────────────────
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── CORS ──────────────────────────────────────────────────────────
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── CSRF ──────────────────────────────────────────────────────────
            // XSRF-TOKEN cookie (HttpOnly=false): React reads it and sends it
            // back as the X-XSRF-TOKEN request header on every mutating call.
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )

            // ── Saved-request cache: cookie-based, no session ─────────────────
            .requestCache(cache -> cache.requestCache(savedRequestRepository))

            // ── Security response headers ─────────────────────────────────────
            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                    .preload(true)
                )
                .contentTypeOptions(c -> {})
                .frameOptions(f -> f.deny())
                .xssProtection(x -> x.disable())
                .referrerPolicy(r ->
                    r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives(props.headers().contentSecurityPolicy())
                )
                .cacheControl(c -> {})
            )

            // ── Authorization rules ───────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                .requestMatchers(
                    "/login/**",
                    "/oauth2/**",
                    "/logout",
                    "/bff/user"
                ).permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )

            // ── OAuth2 login: fully cookie-based ──────────────────────────────
            .oauth2Login(oauth2 -> oauth2
                // Store state + PKCE in a cookie, not the session
                .authorizationEndpoint(ep ->
                    ep.authorizationRequestRepository(authRequestRepository)
                )
                // Store tokens in a cookie, not the session
                .authorizedClientRepository(authorizedClientRepository)
                // After login, redirect to the saved-request URL
                .defaultSuccessUrl("/", false)
            )

            // ── OIDC RP-Initiated Logout ──────────────────────────────────────
            // Invalidates the token cookie AND calls the IdP end_session_endpoint
            // so the user is fully logged out from the SSO realm.
            .logout(logout -> logout
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                .addLogoutHandler((request, response, authentication) ->
                    // Clear the token cookie on logout
                    authorizedClientRepository.removeAuthorizedClient(
                            props.oauth2().registrationId(), authentication, request, response)
                )
            )

            // ── Authentication entry point ─────────────────────────────────────
            // Different behaviour for AJAX calls vs browser navigation:
            //   /api/**  or  Accept: application/json  → 401 JSON
            //   browser navigation                     → 302 to IdP
            .exceptionHandling(ex -> ex
                // For API paths: return 401 JSON
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) -> {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        String loginUrl = "/oauth2/authorization/" + props.oauth2().registrationId();
                        response.getWriter().write(
                            "{\"error\":\"unauthenticated\",\"loginUrl\":\"" + loginUrl + "\"}"
                        );
                    },
                    apiOrJsonMatcher()
                )
                // For browser navigation: 302 redirect to IdP
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint(
                        "/oauth2/authorization/" + props.oauth2().registrationId()
                    ),
                    new AntPathRequestMatcher("/**")
                )
            );

        return http.build();
    }

    // ── CSRF cookie ───────────────────────────────────────────────────────────

    /**
     * XSRF-TOKEN cookie:
     *   HttpOnly = false  → React reads it via document.cookie
     *   Secure   = *      → HTTPS in production
     *   SameSite = Strict → set via SameSiteCookieFilter
     */
    @Bean
    public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookiePath("/");
        repo.setSecure(props.cookie().secure());
        return repo;
    }

    // ── OIDC logout ───────────────────────────────────────────────────────────

    private OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler handler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrations);
        handler.setPostLogoutRedirectUri("{baseUrl}/");
        return handler;
    }

    // ── CORS ──────────────────────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(props.cors().allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Content-Type",
                "Accept",
                "X-XSRF-TOKEN",
                "X-Requested-With",
                "Authorization"
        ));
        config.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Matches requests that should receive a 401 JSON response rather than
     * a browser redirect: /api/** paths, or requests with Accept: application/json
     * but not Accept: text/html.
     */
    private RequestMatcher apiOrJsonMatcher() {
        RequestMatcher apiPath  = new AntPathRequestMatcher("/api/**");
        RequestMatcher jsonType = new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON);
        RequestMatcher htmlType = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
        RequestMatcher jsonNotHtml = new NegatedRequestMatcher(htmlType);

        return request -> apiPath.matches(request)
                || (jsonType.matches(request) && jsonNotHtml.matches(request));
    }
}
