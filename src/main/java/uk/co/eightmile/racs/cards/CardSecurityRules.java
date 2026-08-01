package uk.co.eightmile.racs.cards;

import uk.co.eightmile.racs.auth.SecurityRules;
import uk.co.eightmile.racs.permissions.Authority;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class CardSecurityRules implements SecurityRules {
    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                .requestMatchers(HttpMethod.POST, "/api/card", "/api/card/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_CARD.name()
                )

                .requestMatchers(HttpMethod.GET, "/api/card", "/api/card/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READER.name(),
                        Authority.READ_CARD.name()
                )

                .requestMatchers(HttpMethod.PUT, "/api/card/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_CARD.name()
                )

                .requestMatchers(HttpMethod.DELETE, "/api/card/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.DELETE_CARD.name()
                );
    }
}
