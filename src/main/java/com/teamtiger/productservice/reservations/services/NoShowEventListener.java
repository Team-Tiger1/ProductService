package com.teamtiger.productservice.reservations.services;

import com.teamtiger.productservice.bundles.entities.Bundle;
import com.teamtiger.productservice.bundles.repositories.BundleRepository;
import com.teamtiger.productservice.reservations.config.RabbitMQConfig;
import com.teamtiger.productservice.reservations.entities.Reservation;
import com.teamtiger.productservice.reservations.models.CollectionStatus;
import com.teamtiger.productservice.reservations.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NoShowEventListener {

    private final ReservationRepository reservationRepository;
    private final BundleRepository bundleRepository;

    /**
     * Listens for messages that are sent when a bundle's collection window ends
     * @param bundleId The ID of the bundle on the database
     */
    @RabbitListener(queues = RabbitMQConfig.DELAY_QUEUE)
    public void handleNoShow(UUID bundleId) {

        //Get bundle from database
        Optional<Bundle> bundleOptional = bundleRepository.findById(bundleId);

        if(bundleOptional.isEmpty()) {
            return; //Bundle not found
        }

        Bundle bundle = bundleOptional.get();

        //Get reservation from database
        Optional<Reservation> reservationOptional = reservationRepository.findByBundleId(bundleId);

        if(reservationOptional.isEmpty()) {
            return; //Reservation not found
        }

        Reservation reservation = reservationOptional.get();

        if(reservation.getStatus() == CollectionStatus.RESERVED && bundle.getCollectionEnd().isBefore(LocalDateTime.now())) {
            //Collection Window has Expired and No one has picked uo
            reservation.setStatus(CollectionStatus.NO_SHOW);
            reservationRepository.save(reservation);
        }


    }

}
