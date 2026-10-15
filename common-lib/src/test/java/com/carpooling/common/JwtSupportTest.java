package com.carpooling.common;

import com.carpooling.common.security.JwtSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class JwtSupportTest {

    private static final String VALID_SECRET = "MyVeryLongSecretKeyFor256BitHS256AlgorithmThatIsAtLeast32Characters";

    @Test
    @DisplayName("Deve gerar SecretKey HMAC com segredo válido de 256 bits")
    void testHmacKeyWithValidSecret() {
        SecretKey key = JwtSupport.hmacKey(VALID_SECRET);
        assertNotNull(key);
        assertEquals("HmacSHA256", key.getAlgorithm());
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando segredo for nulo ou vazio")
    void testHmacKeyWithNullOrEmptySecret() {
        assertThrows(IllegalStateException.class, () -> JwtSupport.hmacKey(null));
        assertThrows(IllegalStateException.class, () -> JwtSupport.hmacKey("   "));
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando segredo tiver menos de 32 bytes")
    void testHmacKeyWithShortSecret() {
        assertThrows(IllegalStateException.class, () -> JwtSupport.hmacKey("short-secret-key"));
    }
}
