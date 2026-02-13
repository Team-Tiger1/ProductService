package com.teamtiger.productservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.UUID;

@Component
public class JwtTokenUtil {

    // Load secret from environment variables
    @Value("${jwt.secret}")
    private String key;

    //Extracts vendor/user id from JWT
    public UUID getUuidFromToken(String token) {
        return UUID.fromString(getClaimsFromToken(token).getSubject());
    }
    //Returns role stored in JWT
    public String getRoleFromToken(String token) {
        return (String) getClaimsFromToken(token).get("role");
    }

    //Parses and validates the JWT using the configured key
    private Claims getClaimsFromToken(String token) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        Key hmacKey = new SecretKeySpec(decodedKey, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(60) //Allows 60 seconds after expiry
                .setSigningKey(hmacKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }



}