package uk.co.eightmile.racs.users.specifications;

import uk.co.eightmile.racs.users.dtos.UserRequestQueryParams;
import uk.co.eightmile.racs.users.User;
import org.springframework.data.jpa.domain.Specification;


public class UserSpec {
    public static Specification<User> hasFirstName(String firstName) {
        return ((root, query, cb) ->
                cb.like(cb.lower(root.get("firstName")), "%" + firstName.trim().toLowerCase() + "%"));
    }

    public static Specification<User> hasLastName(String lastName) {
        return ((root, query, cb) ->
                cb.like(cb.lower(root.get("lastName")), "%" + lastName.trim().toLowerCase() + "%"));
    }

    public static Specification<User> hasEmail(String email) {
        return ((root, query, cb) ->
                cb.like(cb.lower(root.get("email")), "%" + email.trim().toLowerCase() + "%"));
    }

    public static Specification<User> isInactive(Boolean inactive) {
        return ((root, query, cb) ->
                cb.equal(root.get("inactive"), inactive));
    }

    public static Specification<User> hasCampaignId(String campaignId) {
        return ((root, query, cb) ->
                cb.equal(root.get("campaign").get("id"), campaignId));
    }

    public static Specification<User> fromQueryParams(UserRequestQueryParams queryParams) {
        Specification<User> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (queryParams.getFirstName() != null && !queryParams.getFirstName().isBlank()) {
            spec = spec.and(hasFirstName(queryParams.getFirstName()));
        }

        if (queryParams.getLastName() != null && !queryParams.getLastName().isBlank()) {
            spec = spec.and(hasLastName(queryParams.getLastName()));
        }

        if (queryParams.getEmail() != null && !queryParams.getEmail().isBlank()) {
            spec = spec.and(hasEmail(queryParams.getEmail()));
        }

        if (queryParams.getInactive() != null) {
            spec = spec.and(isInactive(queryParams.getInactive()));
        }

        if (queryParams.getCampaignId() != null) {
            spec = spec.and(hasCampaignId(queryParams.getCampaignId()));
        }

        return spec;
    }
}
