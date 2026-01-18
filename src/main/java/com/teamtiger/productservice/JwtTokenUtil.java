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
    @Value("${JWT_SECRET}")
    private String key;


    public UUID getUuidFromToken(String token) {
        return UUID.fromString(getClaimsFromToken(token).getSubject());
    }

    public String getRoleFromToken(String token) {
        return (String) getClaimsFromToken(token).get("type");
    }

    private Claims getClaimsFromToken(String token) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        Key hmacKey = new SecretKeySpec(decodedKey, SignatureAlgorithm.HS256.getJcaName());

        return Jwts.parserBuilder()
                .setSigningKey(hmacKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}