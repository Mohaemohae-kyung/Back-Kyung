package kyung.kung_backend.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import kyung.kung_backend.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey key;

    private final long accessTokenExpireTime;

    private final long refreshTokenExpireTime;

    public JwtProvider(

            @Value("${jwt.secret}")
            String secretKey,

            @Value("${jwt.access-expiration-ms}")
            long accessTokenExpireTime,

            @Value("${jwt.refresh-expiration-ms}")
            long refreshTokenExpireTime

    ) {

        this.key =
                Keys.hmacShaKeyFor(
                        secretKey.getBytes(StandardCharsets.UTF_8)
                );

        this.accessTokenExpireTime =
                accessTokenExpireTime;

        this.refreshTokenExpireTime =
                refreshTokenExpireTime;
    }

    public String createAccessToken(User user) {

        Date now = new Date();

        Date expiredAt =
                new Date(now.getTime() + accessTokenExpireTime);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(now)
                .setExpiration(expiredAt)

                .signWith(key, SignatureAlgorithm.HS256)

                .compact();
    }

    public String createRefreshToken(User user) {

        Date now = new Date();

        Date expiredAt =
                new Date(now.getTime() + refreshTokenExpireTime);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiredAt)

                .signWith(key, SignatureAlgorithm.HS256)

                .compact();
    }

    public boolean validateToken(String token) {

        try {

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (JwtException | IllegalArgumentException e) {

            return false;
        }
    }

    public Long getUserId(String token) {

        Claims claims =
                Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

        return Long.valueOf(claims.getSubject());
    }
}