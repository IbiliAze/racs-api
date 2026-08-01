package uk.co.eightmile.racs.readers.specifications;

import uk.co.eightmile.racs.readers.Reader;
import uk.co.eightmile.racs.readers.dtos.ReaderRequestQueryParams;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ReaderSpec {

    public static Specification<Reader> hasUsername(String username) {
        return ((root, query, cb) ->
                cb.like(cb.lower(root.get("username")), "%" + username.trim().toLowerCase() + "%"));
    }

    public static Specification<Reader> hasLocationId(UUID locationId) {
        return ((root, query, cb) ->
                cb.equal(root.get("location").get("id"), locationId));
    }

    public static Specification<Reader> isInactive(Boolean inactive) {
        return ((root, query, cb) ->
                cb.equal(root.get("inactive"), inactive));
    }

    public static Specification<Reader> fromQueryParams(ReaderRequestQueryParams queryParams) {
        Specification<Reader> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (queryParams.getUsername() != null && !queryParams.getUsername().isBlank()) {
            spec = spec.and(hasUsername(queryParams.getUsername()));
        }

        if (queryParams.getLocationId() != null) {
            spec = spec.and(hasLocationId(queryParams.getLocationId()));
        }

        if (queryParams.getInactive() != null) {
            spec = spec.and(isInactive(queryParams.getInactive()));
        }

        return spec;
    }
}