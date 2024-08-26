package com.project.login_api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.project.login_api.modules.user.User;
import com.project.login_api.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${api.security.toke.secret}")
    private String palavraSecreta;

    @Autowired
    private UserRepository userRepository;

    public String generatedToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(palavraSecreta);
            return JWT.create()
                    .withIssuer("login_api")
                    .withSubject(user.getId().toString())
                    .withClaim("roles", Arrays.asList(user.getRole().getRole()))
                    .withExpiresAt(this.generatedExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while authenticationg", exception);
        }
    }

    private Instant generatedExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }

    public String validateToken(String token) {
        token = token.replace("Bearer ", "");
        try {
            Algorithm algorithm = Algorithm.HMAC256(palavraSecreta);
            return JWT.require(algorithm)
                    .withIssuer("login_api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public String tokenRefresh(String token) {
        String subject = validateToken(token);
        if (subject != null) {
            User user = userRepository.findById(UUID.fromString(subject))
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return generatedToken(user);
        }
        throw new RuntimeException("Invalid token, cannot refresh");
    }

}
