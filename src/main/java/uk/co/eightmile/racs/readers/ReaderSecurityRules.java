package uk.co.eightmile.racs.readers;

import uk.co.eightmile.racs.auth.SecurityRules;
import uk.co.eightmile.racs.permissions.Authority;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class ReaderSecurityRules implements SecurityRules {
    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                // Public reader auth
                .requestMatchers(HttpMethod.POST, "/api/reader/token").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/reader/token/refresh").permitAll()

                // Reader auth info
                .requestMatchers(HttpMethod.GET, "/api/reader/auth").authenticated()

                // Create reader
                .requestMatchers(HttpMethod.POST, "/api/reader").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_READER.name()
                )

                // Read readers: GET /api/reader and GET /api/reader/{id}
                .requestMatchers(HttpMethod.GET, "/api/reader", "/api/reader/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READ_READER.name()
                )

                // Update reader: PUT /api/reader/{id}
                .requestMatchers(HttpMethod.PUT, "/api/reader/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_READER.name()
                )

                // Delete reader: DELETE /api/reader/{id}
                .requestMatchers(HttpMethod.DELETE, "/api/reader/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.DELETE_READER.name()
                );
    }
}