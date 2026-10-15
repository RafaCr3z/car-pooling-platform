package com.carpooling.notification.service;

import com.carpooling.notification.entity.Notification;
import com.carpooling.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Deve retornar todas as notificações ordenadas de um utilizador")
    void testGetUserNotifications() {
        Notification n1 = Notification.builder().id(1L).userId(10L).title("Viagem").build();
        Notification n2 = Notification.builder().id(2L).userId(10L).title("Reserva").build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(n1, n2));

        List<Notification> result = notificationService.getUserNotifications(10L);

        assertEquals(2, result.size());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(10L);
    }

    @Test
    @DisplayName("Deve retornar apenas as notificações não lidas de um utilizador")
    void testGetUnreadNotifications() {
        Notification n1 = Notification.builder().id(1L).userId(10L).isRead(false).build();
        when(notificationRepository.findByUserIdAndIsReadFalse(10L)).thenReturn(List.of(n1));

        List<Notification> result = notificationService.getUnreadNotifications(10L);

        assertEquals(1, result.size());
        assertFalse(result.get(0).getIsRead());
        verify(notificationRepository).findByUserIdAndIsReadFalse(10L);
    }

    @Test
    @DisplayName("Deve contar o número exato de notificações não lidas")
    void testGetUnreadCount() {
        when(notificationRepository.countByUserIdAndIsReadFalse(10L)).thenReturn(5L);

        long count = notificationService.getUnreadCount(10L);

        assertEquals(5L, count);
        verify(notificationRepository).countByUserIdAndIsReadFalse(10L);
    }

    @Test
    @DisplayName("Deve marcar uma notificação específica como lida")
    void testMarkAsRead_Success() {
        Notification n = Notification.builder()
                .id(1L)
                .userId(10L)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        Notification updated = notificationService.markAsRead(1L);

        assertTrue(updated.getIsRead());
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(n);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar marcar notificação inexistente como lida")
    void testMarkAsRead_NotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> notificationService.markAsRead(99L));
        verify(notificationRepository).findById(99L);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve marcar todas as notificações não lidas como lidas em lote")
    void testMarkAllAsRead() {
        Notification n1 = Notification.builder().id(1L).userId(10L).isRead(false).build();
        Notification n2 = Notification.builder().id(2L).userId(10L).isRead(false).build();

        when(notificationRepository.findByUserIdAndIsReadFalse(10L)).thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(10L);

        assertTrue(n1.getIsRead());
        assertTrue(n2.getIsRead());
        verify(notificationRepository).saveAll(List.of(n1, n2));
    }

    @Test
    @DisplayName("Deve lidar com lista vazia ao marcar todas como lidas sem lançar erro")
    void testMarkAllAsRead_Empty() {
        when(notificationRepository.findByUserIdAndIsReadFalse(10L)).thenReturn(List.of());

        notificationService.markAllAsRead(10L);

        verify(notificationRepository).saveAll(List.of());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando utilizador não tem notificações não lidas")
    void testGetUnreadNotifications_Empty() {
        when(notificationRepository.findByUserIdAndIsReadFalse(10L)).thenReturn(List.of());

        List<Notification> result = notificationService.getUnreadNotifications(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

