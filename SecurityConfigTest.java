package com.bff;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the BFF security configuration.
 * Uses MockMvc (Servlet stack) – no WebTestClient, no reactive types.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Unauthenticated access ────────────────────────────────────────────────

    @Test
    void unauthenticated_api_call_with_json_accept_returns_401_json() throws Exception {
        mockMvc.perform(get("/api/v1/projets")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("unauthenticated"))
                .andExpect(jsonPath("$.loginUrl").isNotEmpty());
    }

    @Test
    void unauthenticated_browser_navigation_returns_302_to_idp() throws Exception {
        mockMvc.perform(get("/api/v1/projets")
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        org.hamcrest.Matchers.containsString("/oauth2/authorization/")));
    }

    @Test
    void health_endpoint_is_publicly_accessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // ── Security headers ──────────────────────────────────────────────────────

    @Test
    void security_headers_present_on_all_responses() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().exists("Referrer-Policy"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Cache-Control"));
    }

    @Test
    void permissions_policy_header_present() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("Permissions-Policy"));
    }

    @Test
    void server_header_not_exposed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().doesNotExist("Server"))
                .andExpect(header().doesNotExist("X-Powered-By"));
    }

    // ── CSRF cookie ───────────────────────────────────────────────────────────

    @Test
    void xsrf_cookie_present_on_first_request() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));  // must be readable by JS
    }
}
