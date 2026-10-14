package com.carpooling.frontend.service;

import com.carpooling.frontend.dto.NotificationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private WebClient webClient;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Deve retornar lista vazia se o ID do utilizador for nulo")
    void testGetUserNotifications_NullUserId() {
        List<NotificationDTO> result = notificationService.getUserNotifications(null, "token-xyz");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar contagem zero de não lidas se o ID do utilizador for nulo")
    void testGetUnreadCount_NullUserId() {
        long count = notificationService.getUnreadCount(null, "token-xyz");
        assertEquals(0, count);
    }
}
