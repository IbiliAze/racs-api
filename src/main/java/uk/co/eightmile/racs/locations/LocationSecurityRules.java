package uk.co.eightmile.racs.locations;

import uk.co.eightmile.racs.auth.SecurityRules;
import uk.co.eightmile.racs.permissions.Authority;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class LocationSecurityRules implements SecurityRules {
    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                // Create location
                .requestMatchers(HttpMethod.POST, "/api/location").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_LOCATION.name()
                )

                // Read locations: GET /api/location and GET /api/location/{id}
                .requestMatchers(HttpMethod.GET, "/api/location", "/api/location/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READ_LOCATION.name()
                )

                // Update location: PUT /api/location/{id}
                .requestMatchers(HttpMethod.PUT, "/api/location/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_LOCATION.name()
                )

                // Delete location: DELETE /api/location/{id}
                .requestMatchers(HttpMethod.DELETE, "/api/location/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.DELETE_LOCATION.name()
                );

    }
}
