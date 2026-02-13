package com.teamtiger.productservice.reservations.services;

import com.teamtiger.productservice.reservations.config.RabbitMQConfig;
import com.teamtiger.productservice.reservations.models.ReservationCollectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
//Notifies that a reservation has been collected
public class ReservationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishReservationCollected(ReservationCollectedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "reservation.collected", event,
                message -> {
            message.getMessageProperties().setContentType("application/json");
            return message;
                });
    }

}
