package uk.co.eightmile.racs.campaigns;

import uk.co.eightmile.racs.auth.SecurityRules;
import uk.co.eightmile.racs.permissions.Authority;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class CampaignSecurityRules implements SecurityRules {
    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                // Create campaign
                .requestMatchers(HttpMethod.POST, "/api/campaign").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_CAMPAIGN.name()
                )

                // Read campaigns: GET /api/campaign and GET /api/campaign/{id}
                .requestMatchers(HttpMethod.GET, "/api/campaign", "/api/campaign/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READER.name(),
                        Authority.READ_CAMPAIGN.name()
                )

                // Update campaign: PUT /api/campaign/{id}
                .requestMatchers(HttpMethod.PUT, "/api/campaign/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_CAMPAIGN.name()
                )

                // Delete campaign: DELETE /api/campaign/{id}
                .requestMatchers(HttpMethod.DELETE, "/api/campaign/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.DELETE_CAMPAIGN.name()
                );
    }
}
