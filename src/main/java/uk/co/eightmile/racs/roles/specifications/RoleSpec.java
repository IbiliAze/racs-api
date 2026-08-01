package uk.co.eightmile.racs.roles.specifications;

import uk.co.eightmile.racs.roles.Role;
import uk.co.eightmile.racs.roles.dtos.RoleRequestQueryParams;
import org.springframework.data.jpa.domain.Specification;

public class RoleSpec {
    public static Specification<Role> hasName(String name) {
        return ((root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
    }

    public static Specification<Role> fromQueryParams(RoleRequestQueryParams queryParams) {
        Specification<Role> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (queryParams.getName() != null && !queryParams.getName().isBlank()) {
            spec = spec.and(hasName(queryParams.getName()));
        }

        return spec;
    }
}
