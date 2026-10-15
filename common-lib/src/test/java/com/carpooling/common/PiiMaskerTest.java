package com.carpooling.common;

import com.carpooling.common.dto.UserSummaryDTO;
import com.carpooling.common.privacy.PiiMasker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PiiMaskerTest {

    @Test
    @DisplayName("Deve mascarar email mantendo apenas primeiro e último caracter do nome de utilizador")
    void testMaskEmail() {
        assertEquals("j********a@example.com", PiiMasker.maskEmail("joao.silva@example.com"));
        assertEquals("a***@test.com", PiiMasker.maskEmail("ab@test.com"));
        assertNull(PiiMasker.maskEmail(null));
        assertEquals("", PiiMasker.maskEmail(""));
    }

    @Test
    @DisplayName("Deve mascarar número de telefone mantendo indicativo e últimos 3 dígitos")
    void testMaskPhone() {
        assertEquals("+351******678", PiiMasker.maskPhone("+351912345678"));
        assertNull(PiiMasker.maskPhone(null));
        assertEquals("", PiiMasker.maskPhone(""));
    }

    @Test
    @DisplayName("Deve mascarar UserSummaryDTO sem alterar campos não sensíveis")
    void testMaskUserSummaryDTO() {
        UserSummaryDTO user = UserSummaryDTO.builder()
                .id(10L)
                .email("admin@carpooling.pt")
                .fullName("Administrador Geral")
                .phoneNumber("+351987654321")
                .role("ADMIN")
                .rating(5.0)
                .ratingCount(12)
                .isBlocked(false)
                .build();

        UserSummaryDTO masked = PiiMasker.mask(user);

        assertNotNull(masked);
        assertEquals(10L, masked.getId());
        assertEquals("Administrador Geral", masked.getFullName());
        assertEquals("ADMIN", masked.getRole());
        assertEquals("a***n@carpooling.pt", masked.getEmail());
        assertEquals("+351******321", masked.getPhoneNumber());
        assertEquals(5.0, masked.getRating());
        assertFalse(masked.getIsBlocked());
    }

    @Test
    @DisplayName("Deve retornar null ao mascarar DTO nulo")
    void testMaskNullDTO() {
        assertNull(PiiMasker.mask(null));
    }
}
