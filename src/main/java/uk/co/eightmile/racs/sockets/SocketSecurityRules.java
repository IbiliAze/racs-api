package uk.co.eightmile.racs.sockets;

import uk.co.eightmile.racs.auth.SecurityRules;
import uk.co.eightmile.racs.permissions.Authority;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class SocketSecurityRules implements SecurityRules {
    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                .requestMatchers(HttpMethod.GET, "/rtc").permitAll()
                .requestMatchers(HttpMethod.GET, "/ws/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/ws/mesh").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READER.name()
                );
    }
}