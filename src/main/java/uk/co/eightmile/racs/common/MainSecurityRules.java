package uk.co.eightmile.racs.common;

import uk.co.eightmile.racs.auth.SecurityRules;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class MainSecurityRules implements SecurityRules {
    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                // Read health
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                // Actuator health probes, used by the container healthcheck;
                // not exposed publicly — the reverse proxy only forwards /api,
                // /swagger-ui and /v3/api-docs to this service.
                .requestMatchers(HttpMethod.GET, "/actuator/health/**").permitAll();
    }
}