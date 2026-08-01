package uk.co.eightmile.racs.campaigns.specifications;

import uk.co.eightmile.racs.campaigns.Campaign;
import uk.co.eightmile.racs.campaigns.dtos.CampaignRequestQueryParams;
import org.springframework.data.jpa.domain.Specification;

public class CampaignSpec {

    public static Specification<Campaign> hasName(String name) {
        return ((root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
    }

    public static Specification<Campaign> fromQueryParams(CampaignRequestQueryParams queryParams) {
        Specification<Campaign> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (queryParams.getName() != null && !queryParams.getName().isBlank()) {
            spec = spec.and(hasName(queryParams.getName()));
        }

        return spec;
    }
}
