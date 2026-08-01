package uk.co.eightmile.racs.locations.specifications;

import uk.co.eightmile.racs.locations.Location;
import uk.co.eightmile.racs.locations.dtos.LocationRequestQueryParams;
import org.springframework.data.jpa.domain.Specification;

public class LocationSpec {
    public static Specification<Location> hasName(String name) {
        return ((root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
    }

    public static Specification<Location> isInactive(Boolean inactive) {
        return ((root, query, cb) ->
                cb.equal(root.get("inactive"), inactive));
    }

    public static Specification<Location> fromQueryParams(LocationRequestQueryParams queryParams) {
        Specification<Location> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (queryParams.getName() != null && !queryParams.getName().isBlank()) {
            spec = spec.and(hasName(queryParams.getName()));
        }

        if (queryParams.getInactive() != null) {
            spec = spec.and(isInactive(queryParams.getInactive()));
        }

        return spec;
    }
}
