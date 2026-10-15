package com.carpooling.common;

import com.carpooling.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários - GlobalExceptionHandler (@RestControllerAdvice Centralizado)")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Deve tratar ResponseStatusException mantendo status code e mensagem descritiva")
    void testHandleResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Recurso não encontrado");
        ResponseEntity<ApiResponse<Void>> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Recurso não encontrado", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve tratar IllegalArgumentException com HTTP 400 Bad Request")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Parâmetro de consulta inválido");
        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Parâmetro de consulta inválido", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve tratar IllegalStateException com HTTP 409 Conflict")
    void testHandleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Viagem já lotada ou cancelada");
        ResponseEntity<ApiResponse<Void>> response = handler.handleIllegalStateException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Viagem já lotada ou cancelada", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve tratar AccessDeniedException com HTTP 403 Forbidden")
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Privilégios insuficientes");
        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDeniedException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Acesso negado"));
    }

    @Test
    @DisplayName("Deve tratar exceção genérica com HTTP 500 e mensagem amigável sem expor stacktrace")
    void testHandleGenericException() {
        RuntimeException ex = new RuntimeException("Falha inesperada no driver SQL");
        ResponseEntity<ApiResponse<Void>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("erro interno"));
        assertFalse(response.getBody().getMessage().contains("Falha inesperada no driver SQL"), "Não deve expor detalhes internos do erro");
    }

    @Test
    @DisplayName("Deve tratar MethodArgumentNotValidException agregando erros de campo")
    void testHandleValidationException() throws NoSuchMethodException {
        Method method = this.getClass().getDeclaredMethod("setUp");
        MethodParameter parameter = new MethodParameter(method, -1);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "seats", "deve ser maior que zero"));
        bindingResult.addError(new FieldError("target", "email", "formato de email inválido"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("seats: deve ser maior que zero"));
        assertTrue(response.getBody().getMessage().contains("email: formato de email inválido"));
    }
}
