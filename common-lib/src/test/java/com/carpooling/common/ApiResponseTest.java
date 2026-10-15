package com.carpooling.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - ApiResponse (Estrutura Comum de Resposta REST)")
class ApiResponseTest {

    @Test
    @DisplayName("Deve construir resposta de sucesso com timestamp preenchido")
    void testSuccessResponse() {
        ApiResponse<String> response = new ApiResponse<>(true, "Operação realizada com sucesso", "payload-data");

        assertTrue(response.isSuccess());
        assertEquals("Operação realizada com sucesso", response.getMessage());
        assertEquals("payload-data", response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    @DisplayName("Deve construir resposta de erro com flag de sucesso falsa")
    void testErrorResponse() {
        ApiResponse<Void> response = new ApiResponse<>(false, "Registo não encontrado", null);

        assertFalse(response.isSuccess());
        assertEquals("Registo não encontrado", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    @DisplayName("Deve suportar getters e setters através do Lombok")
    void testGettersAndSetters() {
        ApiResponse<Integer> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage("OK");
        response.setData(42);
        response.setTimestamp(123456789L);

        assertTrue(response.isSuccess());
        assertEquals("OK", response.getMessage());
        assertEquals(42, response.getData());
        assertEquals(123456789L, response.getTimestamp());
    }
}
