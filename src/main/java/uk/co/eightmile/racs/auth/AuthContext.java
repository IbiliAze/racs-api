package uk.co.eightmile.racs.auth;

import uk.co.eightmile.racs.auth.dtos.JwtPrincipalDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthContext {
    public UUID getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        if (authentication.getDetails() instanceof JwtPrincipalDto principal) {
            return principal.getId();
        }

        return null;
    }

    public String getCampaignId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        assert authentication != null;
        if (authentication.getDetails() instanceof JwtPrincipalDto principal) {
            return principal.getCampaignId();
        }

        return null;
    }
}