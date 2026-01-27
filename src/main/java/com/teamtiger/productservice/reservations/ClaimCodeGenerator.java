package com.teamtiger.productservice.reservations;

import com.teamtiger.productservice.reservations.repositories.ClaimCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class ClaimCodeGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ClaimCodeRepository claimCodeRepository;

    @Transactional
    public String generateCode() {
        String claimCode = getCandidate();

        if(!claimCodeRepository.existsByClaimCode(claimCode)) {
            return claimCode;
        }
        return getCandidate();
    }

    private String getCandidate() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            int index =  RANDOM.nextInt(CHARACTERS.length());
            builder.append(CHARACTERS.charAt(index));
        }
        return builder.toString();
    }


}
