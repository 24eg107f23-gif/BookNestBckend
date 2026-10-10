package com.booknest.config;

import com.booknest.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final javax.crypto.SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs,
                      Environment environment) {
        if (secret.isBlank()) {
            if (environment.acceptsProfiles(Profiles.of("mysql"))) {
                throw new IllegalStateException("Set JWT_SECRET to at least 32 bytes when using the MySQL profile.");
            }
            this.key = loadOrCreateLocalKey();
        } else {
            byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes.");
            }
            this.key = Keys.hmacShaKeyFor(bytes);
        }
        this.expirationMs = expirationMs;
    }

    private javax.crypto.SecretKey loadOrCreateLocalKey() {
        Path keyFile = Path.of("data", "jwt.secret");
        try {
            Files.createDirectories(keyFile.getParent());
            if (Files.exists(keyFile)) {
                return Keys.hmacShaKeyFor(Base64.getDecoder().decode(Files.readString(keyFile).trim()));
            }

            javax.crypto.SecretKey generatedKey = Jwts.SIG.HS256.key().build();
            String encodedKey = Base64.getEncoder().encodeToString(generatedKey.getEncoded());
            try {
                Files.writeString(keyFile, encodedKey, StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW);
                return generatedKey;
            } catch (FileAlreadyExistsException exception) {
                return Keys.hmacShaKeyFor(Base64.getDecoder().decode(Files.readString(keyFile).trim()));
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Could not read or create the local JWT signing key in backend/data.", exception);
        }
    }

    public String generateToken(AppUser user) {
        Date now = new Date();
        return Jwts.builder().subject(user.getEmail()).claim("role", user.getRole().name())
                .issuedAt(now).expiration(new Date(now.getTime() + expirationMs)).signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
