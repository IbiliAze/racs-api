package uk.co.eightmile.racs.cards.specifications;

import uk.co.eightmile.racs.cards.Card;
import uk.co.eightmile.racs.cards.CardType;
import uk.co.eightmile.racs.cards.dtos.CardRequestQueryParams;
import uk.co.eightmile.racs.reports.Status;
import uk.co.eightmile.racs.scans.FlagType;
import uk.co.eightmile.racs.scans.Scan;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

public class CardSpec {
    private static final List<FlagType> PASSING_FLAGS = List.of(FlagType.PASSED_OK, FlagType.SIMILARITY_CHECK);

    public static Specification<Card> hasValue(String value) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("value")), "%" + value.trim().toLowerCase() + "%");
    }

    public static Specification<Card> hasType(CardType type) {
        return (root, query, cb) ->
                cb.equal(root.get("type"), type);
    }

    public static Specification<Card> hasCampaignId(String campaignId) {
        return (root, query, cb) ->
                cb.equal(root.get("campaign").get("id"), campaignId);
    }

    public static Specification<Card> hasUsed(boolean used) {
        return (root, query, cb) -> {
            var sub = query.subquery(Long.class);
            var scan = sub.from(Scan.class);
            sub.select(cb.count(scan))
               .where(cb.and(
                   cb.equal(scan.get("card"), root),
                   scan.get("flag").in(PASSING_FLAGS)
               ));
            return used ? cb.gt(sub, 0L) : cb.equal(sub, 0L);
        };
    }

    public static Specification<Card> hasInvalidated(boolean invalidated) {
        return (root, query, cb) ->
                cb.equal(root.get("invalidated"), invalidated);
    }

    public static Specification<Card> hasUsedAtAfter(Instant startDate) {
        return (root, query, cb) -> {
            var sub = query.subquery(Long.class);
            var scan = sub.from(Scan.class);
            sub.select(cb.count(scan))
               .where(cb.and(
                   cb.equal(scan.get("card"), root),
                   scan.get("flag").in(PASSING_FLAGS),
                   cb.greaterThanOrEqualTo(scan.get("createdAt"), startDate)
               ));
            return cb.gt(sub, 0L);
        };
    }

    public static Specification<Card> hasUsedAtBefore(Instant endDate) {
        return (root, query, cb) -> {
            var sub = query.subquery(Long.class);
            var scan = sub.from(Scan.class);
            sub.select(cb.count(scan))
               .where(cb.and(
                   cb.equal(scan.get("card"), root),
                   scan.get("flag").in(PASSING_FLAGS),
                   cb.lessThanOrEqualTo(scan.get("createdAt"), endDate)
               ));
            return cb.gt(sub, 0L);
        };
    }

    public static Specification<Card> fromQueryParams(CardRequestQueryParams params) {
        Specification<Card> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (params.getValue() != null && !params.getValue().isBlank()) {
            spec = spec.and(hasValue(params.getValue()));
        }

        if (params.getType() != null && !params.getType().isBlank()) {
            spec = spec.and(hasType(CardType.valueOf(params.getType().toUpperCase())));
        }

        if (params.getCampaignId() != null) {
            spec = spec.and(hasCampaignId(params.getCampaignId()));
        }

        if (params.getUsed() != null) {
            spec = spec.and(hasUsed(params.getUsed()));
        }

        if (params.getInvalidated() != null) {
            spec = spec.and(hasInvalidated(params.getInvalidated()));
        }

        return spec;
    }

    public static Specification<Card> forCardReport(
            Instant startDate,
            Instant endDate,
            Status status,
            String cardValue
    ) {
        Specification<Card> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (Status.USED.equals(status))     spec = spec.and(hasUsed(true));
        if (Status.NOT_USED.equals(status)) spec = spec.and(hasUsed(false));
        if (startDate != null) spec = spec.and(hasUsedAtAfter(startDate));
        if (endDate != null)   spec = spec.and(hasUsedAtBefore(endDate));
        if (cardValue != null && !cardValue.isBlank()) spec = spec.and(hasValue(cardValue));

        return spec;
    }
}