package uk.co.eightmile.racs.auth;

import uk.co.eightmile.racs.permissions.Authority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Jwt {
    private final Claims claims;
    private final SecretKey secretKey;

    public Jwt(Claims claims, SecretKey secretKey) {
        this.claims = claims;
        this.secretKey = secretKey;
    }

    public Boolean isExpired() {
        return claims.getExpiration().before(new Date());
    }

    public UUID getPrincipalId() {
        return UUID.fromString(claims.getSubject());
    }

    public List<Authority> getAuthorities() {
        var authorities = claims.get("authorities", List.class);

        if (authorities == null) {
            return List.of();
        }

        return ((List<?>) authorities).stream()
                .map(authority -> Authority.valueOf(authority.toString()))
                .toList();}

    public String toString() {
        return Jwts.builder()
                .claims(claims).signWith(secretKey).compact();
    }
}
