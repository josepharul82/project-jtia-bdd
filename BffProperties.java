package com.bff.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Type-safe, validated configuration for all BFF-specific properties.
 * The application refuses to start if any required value is absent or invalid.
 */
@Validated
@ConfigurationProperties(prefix = "bff")
public record BffProperties(

        Oauth2    oauth2,
        Cookie    cookie,
        Cors      cors,
        Proxy     proxy,
        Headers   headers

) {
    public record Oauth2(
            /** Must match spring.security.oauth2.client.registration.<id> */
            @NotBlank String registrationId
    ) {}

    public record Cookie(
            /**
             * AES-256 encryption key for all HttpOnly cookies.
             * Must be exactly 32 bytes encoded as Base64.
             * Generate with: openssl rand -base64 32
             * NEVER commit – inject via environment variable or secrets manager.
             */
            @NotBlank @Size(min = 32) String encryptionKey,

            /** false ONLY for local HTTP development, true everywhere else */
            boolean secure,

            @NotBlank String tokenCookieName,
            @NotBlank String csrfCookieName,
            @NotBlank String savedRequestCookieName,

            /** Cookie max-age in seconds – align with access-token TTL on the IdP */
            @Positive int maxAgeSeconds
    ) {}

    public record Cors(
            @NotEmpty List<@NotBlank String> allowedOrigins
    ) {}

    public record Proxy(
            /** Base URL of the Spring Boot backend */
            @NotBlank String backendUrl,

            /** Connect timeout in milliseconds */
            @Positive int connectTimeoutMs,

            /** Read timeout in milliseconds */
            @Positive int readTimeoutMs
    ) {}

    public record Headers(
            @NotBlank String contentSecurityPolicy,
            @NotBlank String permissionsPolicy
    ) {}
}
