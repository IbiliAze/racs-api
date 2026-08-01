package uk.co.eightmile.racs.roles;

import uk.co.eightmile.racs.auth.SecurityRules;
import uk.co.eightmile.racs.permissions.Authority;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

@Component
public class RoleSecurityRules implements SecurityRules {
    @Override
    public void configure(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry
    ) {
        registry
                // Create role: POST /api/role
                .requestMatchers(HttpMethod.POST, "/api/role").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.CREATE_ROLE.name()
                )

                // Add permissions to role: POST /api/role/{id}/permissions
                .requestMatchers(HttpMethod.POST, "/api/role/{id}/permissions").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_ROLE.name()
                )

                // Read roles: GET /api/role, /api/role/{id}, /api/role/{id}/permissions
                .requestMatchers(HttpMethod.GET, "/api/role", "/api/role/**").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.READ_ROLE.name()
                )

                // Update role: PUT /api/role/{id}
                .requestMatchers(HttpMethod.PUT, "/api/role/{id}").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_ROLE.name()
                )

                // Delete role: DELETE /api/role/{id}
                .requestMatchers(HttpMethod.DELETE, "/api/role/{id}").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.DELETE_ROLE.name()
                )

                // Remove permission from role: DELETE /api/role/{id}/permission/{permissionId}
                .requestMatchers(HttpMethod.DELETE, "/api/role/{id}/permission/{permissionId}").hasAnyAuthority(
                        Authority.ADMIN.name(),
                        Authority.UPDATE_ROLE.name()
                );
    }
}