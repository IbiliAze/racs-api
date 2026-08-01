package uk.co.eightmile.racs.scans;

import uk.co.eightmile.racs.auth.SecurityRules;
import uk.co.eightmile.racs.permissions.Authority;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ScanSecurityRules implements SecurityRules {
    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                // Create scan
                .requestMatchers(HttpMethod.POST, "/api/scan").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READER.name(),
                        Authority.CREATE_SCAN.name()
                )

                // Read scans: GET /api/scan and GET /api/scan/{id}
                .requestMatchers(HttpMethod.GET, "/api/scan", "/api/scan/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READER.name(),
                        Authority.READ_SCAN.name()
                )

                // Update scan: PUT /api/scan/{id}
                .requestMatchers(HttpMethod.PUT, "/api/scan/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_SCAN.name()
                )

                // Delete scan: DELETE /api/scan/{id}
                .requestMatchers(HttpMethod.DELETE, "/api/scan/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.DELETE_SCAN.name()
                );

    }
}
