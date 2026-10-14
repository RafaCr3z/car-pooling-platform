package com.carpooling.notification.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMQConfigTest {

    private final RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();

    @Test
    @DisplayName("Deve configurar exchange principal e exchange de Dead Letter (DLX)")
    void testExchanges() {
        TopicExchange topicExchange = rabbitMQConfig.topicExchange();
        assertEquals("carpooling.topic", topicExchange.getName());

        TopicExchange dlxExchange = rabbitMQConfig.deadLetterExchange();
        assertEquals("carpooling.dlx", dlxExchange.getName());
    }

    @Test
    @DisplayName("Deve configurar filas com argumentos para Dead Letter Exchange (DLQ)")
    void testQueuesWithDeadLetterConfig() {
        Queue tripQueue = rabbitMQConfig.tripQueue();
        assertEquals("notification.trip.queue", tripQueue.getName());
        assertTrue(tripQueue.isDurable());
        assertEquals("carpooling.dlx", tripQueue.getArguments().get("x-dead-letter-exchange"));
        assertEquals("notification.dlq", tripQueue.getArguments().get("x-dead-letter-routing-key"));

        Queue reservationQueue = rabbitMQConfig.reservationQueue();
        assertEquals("notification.reservation.queue", reservationQueue.getName());
        assertTrue(reservationQueue.isDurable());
        assertEquals("carpooling.dlx", reservationQueue.getArguments().get("x-dead-letter-exchange"));
        assertEquals("notification.dlq", reservationQueue.getArguments().get("x-dead-letter-routing-key"));

        Queue dlq = rabbitMQConfig.deadLetterQueue();
        assertEquals("notification.dlq", dlq.getName());
        assertTrue(dlq.isDurable());
    }

    @Test
    @DisplayName("Deve configurar bindings de tópicos e DLQ")
    void testBindings() {
        TopicExchange topicExchange = rabbitMQConfig.topicExchange();
        TopicExchange dlxExchange = rabbitMQConfig.deadLetterExchange();
        Queue tripQueue = rabbitMQConfig.tripQueue();
        Queue resQueue = rabbitMQConfig.reservationQueue();
        Queue dlq = rabbitMQConfig.deadLetterQueue();

        Binding tripBinding = rabbitMQConfig.tripBinding(tripQueue, topicExchange);
        assertEquals("trip.#", tripBinding.getRoutingKey());

        Binding resBinding = rabbitMQConfig.reservationBinding(resQueue, topicExchange);
        assertEquals("reservation.#", resBinding.getRoutingKey());

        Binding dlqBinding = rabbitMQConfig.dlqBinding(dlq, dlxExchange);
        assertEquals("#", dlqBinding.getRoutingKey());
    }

    @Test
    @DisplayName("Deve inicializar MessageConverter com suporte a JSON")
    void testMessageConverter() {
        MessageConverter converter = rabbitMQConfig.messageConverter();
        assertNotNull(converter);
    }
}
