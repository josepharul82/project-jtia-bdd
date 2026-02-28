package com.bff.oauth2;

import com.bff.properties.BffProperties;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * Encrypts and decrypts OAuth2 token payloads into HttpOnly Servlet cookies
 * using AES-256-GCM (JWE direct-key encryption).
 *
 * All methods are plain synchronous Java — no Mono, no Flux.
 *
 * Security guarantees:
 *   • Confidentiality – token values are never visible in the cookie
 *   • Integrity       – AES-GCM detects any tampering before decryption
 *   • XSS protection  – HttpOnly prevents JS from reading the cookie
 *   • MITM protection – Secure flag ensures HTTPS-only transmission
 */
@Slf4j
@Component
public class TokenCookieManager {

    private final BffProperties props;
    private final SecretKey     encryptionKey;

    public TokenCookieManager(BffProperties props) {
        this.props         = props;
        this.encryptionKey = buildKey(props.cookie().encryptionKey());
    }

    // ── Write cookies to response ─────────────────────────────────────────────

    /**
     * Writes the encrypted token bundle into a BFF_TOKEN HttpOnly cookie.
     *
     * @param payload  plaintext JSON to encrypt, e.g. {"accessToken":"…","refreshToken":"…"}
     * @param response Servlet response to write the cookie into
     */
    public void writeTokenCookie(String payload, HttpServletResponse response) {
        String encrypted = encrypt(payload);
        response.addCookie(buildCookie(
                props.cookie().tokenCookieName(),
                encrypted,
                props.cookie().maxAgeSeconds()
        ));
    }

    /**
     * Clears the token cookie (used on logout).
     */
    public void clearTokenCookie(HttpServletResponse response) {
        response.addCookie(buildCookie(props.cookie().tokenCookieName(), "", 0));
    }

    /**
     * Writes an encrypted value into the saved-request cookie (5-minute TTL).
     */
    public void writeSavedRequestCookie(String value, HttpServletResponse response) {
        String encrypted = encrypt(value);
        response.addCookie(buildCookie(
                props.cookie().savedRequestCookieName(),
                encrypted,
                5 * 60  // 5 minutes – just long enough for the login flow
        ));
    }

    /**
     * Clears the saved-request cookie.
     */
    public void clearSavedRequestCookie(HttpServletResponse response) {
        response.addCookie(buildCookie(props.cookie().savedRequestCookieName(), "", 0));
    }

    // ── Read cookies from request ─────────────────────────────────────────────

    /**
     * Reads and decrypts the token cookie from the request.
     *
     * @return decrypted plaintext, or empty if cookie absent / decryption fails
     */
    public Optional<String> readTokenCookie(HttpServletRequest request) {
        return readCookie(request, props.cookie().tokenCookieName())
                .map(this::decrypt);
    }

    /**
     * Reads and decrypts the saved-request cookie from the request.
     *
     * @return decrypted plaintext, or empty if cookie absent / decryption fails
     */
    public Optional<String> readSavedRequestCookie(HttpServletRequest request) {
        return readCookie(request, props.cookie().savedRequestCookieName())
                .map(this::decrypt);
    }

    // ── Encryption ────────────────────────────────────────────────────────────

    /**
     * AES-256-GCM encryption using Nimbus JOSE (JWE compact serialization).
     */
    public String encrypt(String plaintext) {
        try {
            JWEObject jwe = new JWEObject(
                    new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A256GCM),
                    new Payload(plaintext)
            );
            jwe.encrypt(new DirectEncrypter(encryptionKey));
            return jwe.serialize();
        } catch (JOSEException e) {
            log.error("Cookie encryption failed", e);
            throw new IllegalStateException("Failed to encrypt cookie", e);
        }
    }

    /**
     * AES-256-GCM decryption. Returns null on any error (expired, tampered, wrong key).
     */
    public String decrypt(String jweCompact) {
        try {
            JWEObject jwe = JWEObject.parse(jweCompact);
            jwe.decrypt(new DirectDecrypter(encryptionKey));
            return jwe.getPayload().toString();
        } catch (ParseException | JOSEException e) {
            log.debug("Cookie decryption failed (may be expired or tampered): {}", e.getMessage());
            return null;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Optional<String> readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst();
    }

    private Cookie buildCookie(String name, String value, int maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);                          // JS cannot read it
        cookie.setSecure(props.cookie().secure());         // HTTPS only
        cookie.setPath("/");                               // whole BFF
        cookie.setMaxAge(maxAgeSeconds);                   // 0 = delete immediately
        // SameSite must be set via the header attribute (Servlet API < 6 has no setter)
        // Spring Boot's Tomcat/Jetty/Undertow pick this up via the SameSiteCookieFilter
        return cookie;
    }

    private static SecretKey buildKey(String base64Key) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "bff.cookie.encryption-key must be a Base64-encoded 32-byte key. "
                  + "Generate with: openssl rand -base64 32");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
}
