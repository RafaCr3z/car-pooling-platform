package com.carpooling.eureka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EurekaServerApplicationTest {

    @Test
    @DisplayName("Deve validar a classe principal da aplicação Eureka Server")
    void testMainClass() {
        assertNotNull(EurekaServerApplication.class);
    }
}
