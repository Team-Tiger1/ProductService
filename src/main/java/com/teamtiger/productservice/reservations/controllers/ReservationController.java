package com.teamtiger.productservice.reservations.controllers;

import com.teamtiger.productservice.reservations.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private ReservationService reservationService;

}
