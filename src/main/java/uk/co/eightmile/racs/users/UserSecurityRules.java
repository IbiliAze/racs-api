package uk.co.eightmile.racs.users;

import uk.co.eightmile.racs.auth.SecurityRules;
import uk.co.eightmile.racs.permissions.Authority;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class UserSecurityRules implements SecurityRules {
    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                // Public auth routes
                .requestMatchers(HttpMethod.POST, "/api/user/token").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/user/token/refresh").permitAll()

                // Create user
                .requestMatchers(HttpMethod.POST, "/api/user/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/user/register").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_USER.name()
                )
                .requestMatchers(HttpMethod.POST, "/api/user").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_USER.name()
                )

                // Add roles to user
                .requestMatchers(HttpMethod.POST, "/api/user/{id}/roles").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_USER.name()
                )

                // Logged in user info
                .requestMatchers(HttpMethod.GET, "/api/user/auth").authenticated()

                // Read users: GET /api/user
                .requestMatchers(HttpMethod.GET, "/api/user").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READ_USER.name()
                )

                // Read user by ID: GET /api/user/{id}
                .requestMatchers(HttpMethod.GET, "/api/user/{id}").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READ_USER.name()
                )

                // Read user roles: GET /api/user/{id}/roles
                .requestMatchers(HttpMethod.GET, "/api/user/{id}/roles").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READ_USER.name()
                )

                // Read user permissions: GET /api/user/{id}/permissions
                .requestMatchers(HttpMethod.GET, "/api/user/{id}/permissions").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READ_USER.name()
                )

                // Update user: PUT /api/user/{id}
                .requestMatchers(HttpMethod.PUT, "/api/user/{id}").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_USER.name()
                )

                // Delete user: DELETE /api/user/{id}
                .requestMatchers(HttpMethod.DELETE, "/api/user/{id}").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.DELETE_USER.name()
                )

                // Remove role from user: DELETE /api/user/{id}/role/{roleId}
                .requestMatchers(HttpMethod.DELETE, "/api/user/{id}/role/{roleId}").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_USER.name()
                );
    }
}