package com.teamtiger.productservice.reservations.services;

import com.teamtiger.productservice.reservations.config.RabbitMQConfig;
import com.teamtiger.productservice.reservations.models.ReservationCollectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Notifies that a reservation has been collected
 */
@Service
@RequiredArgsConstructor
public class ReservationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishReservationCollected(ReservationCollectedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "reservation.collected", event,
                message -> {
            message.getMessageProperties().setContentType("application/json");
            return message;
                });
    }

    public void publishNoShowEvent(UUID bundleId, long delay) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAY_EXCHANGE,
                RabbitMQConfig.DELAY_ROUTING_KEY,
                bundleId,
                message -> {
                    message.getMessageProperties().setDelayLong(delay + 100); //+100 ms ensures message is picked up after window has ended
                    return message;
                }
        );
    }

}
