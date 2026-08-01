package uk.co.eightmile.racs.auth;

import uk.co.eightmile.racs.auth.config.JwtConfig;
import uk.co.eightmile.racs.auth.dtos.JwtPrincipalDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@AllArgsConstructor
public class JwtService {
    private final JwtConfig jwtConfig;

    public Jwt generateAccessToken(JwtPrincipalDto jwtPrincipalDto) {
        return generateToken(jwtPrincipalDto, jwtConfig.getAccessTokenExpiration());
    }

    public Jwt generateReaderAccessToken(JwtPrincipalDto jwtPrincipalDto) {
        return generateToken(
                jwtPrincipalDto,
                jwtConfig.getReaderAccessTokenExpiration()
        );
    }

    public Jwt generateRefreshToken(JwtPrincipalDto jwtPrincipalDto) {
        return generateToken(jwtPrincipalDto, jwtConfig.getRefreshTokenExpiration());
    }

    private Jwt generateToken(JwtPrincipalDto jwtPrincipalDto, long tokenExpiration) {
        var claimsBuilder = Jwts.claims()
                .subject(jwtPrincipalDto.getId().toString())
                .add("email", jwtPrincipalDto.getEmail())
                .add("firstName", jwtPrincipalDto.getFirstName())
                .add("lastName", jwtPrincipalDto.getLastName())
                .add("authorities", jwtPrincipalDto.getAuthorities());

        if (jwtPrincipalDto.getCampaignId() != null) {
            claimsBuilder.add("campaignId", jwtPrincipalDto.getCampaignId());
        }

        Claims claims = claimsBuilder
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .build();

        return new Jwt(claims, jwtConfig.getSecretKey());
    }

    public Jwt parseToken(String token) {
        try {
            var claims = getClaims(token);
            return new Jwt(claims, jwtConfig.getSecretKey());
        } catch (JwtException e) {
            return null;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
