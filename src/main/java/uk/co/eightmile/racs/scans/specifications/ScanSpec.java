package uk.co.eightmile.racs.scans.specifications;

import uk.co.eightmile.racs.scans.FlagType;
import uk.co.eightmile.racs.scans.dtos.ScanRequestQueryParams;
import uk.co.eightmile.racs.scans.Scan;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ScanSpec {
    public static Specification<Scan> hasScannedValue(String scannedValue) {
        return ((root, query, cb) ->
                cb.like(cb.lower(root.get("scannedValue")), "%" + scannedValue.trim().toLowerCase() + "%"));
    }

    public static Specification<Scan> hasFlag(String flag) {
        return (root, query, cb) ->
                cb.equal(root.get("flag"), FlagType.fromQueryValue(flag));
    }

    public static Specification<Scan> hasReaderId(UUID readerId) {
        return ((root, query, cb) ->
                cb.equal(root.get("reader").get("id"), readerId));
    }

    public static Specification<Scan> hasFlagsIn(List<FlagType> flags) {
        return (root, query, cb) ->
                root.get("flag").in(flags);
    }

    public static Specification<Scan> hasLocationId(UUID locationId) {
        return (root, query, cb) ->
                cb.equal(root.get("reader").get("location").get("id"), locationId);
    }

    public static Specification<Scan> hasStartDate(Instant startDate) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
    }

    public static Specification<Scan> hasEndDate(Instant endDate) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
    }

    public static Specification<Scan> withCard() {
        return (root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("card", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Scan> fromQueryParams(
            ScanRequestQueryParams queryParams, boolean withCard) {
        Specification<Scan> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (queryParams.getScannedValue() != null && !queryParams.getScannedValue().isBlank()) {
            spec = spec.and(hasScannedValue(queryParams.getScannedValue()));
        }

        if (queryParams.getFlag() != null && !queryParams.getFlag().isBlank()) {
            spec = spec.and(hasFlag(queryParams.getFlag()));
        }

        if (queryParams.getReaderId() != null) {
            spec = spec.and(hasReaderId(queryParams.getReaderId()));
        }

        if (withCard) {
            spec = spec.and(withCard());
        }

        return spec;
    }

    public static Specification<Scan> forLocationReport(
            List<FlagType> flags,
            UUID locationId,
            Instant startDate,
            Instant endDate
    ) {
        Specification<Scan> spec = Specification.where(hasFlagsIn(flags));

        if (locationId != null) spec = spec.and(hasLocationId(locationId));
        if (startDate != null) spec = spec.and(hasStartDate(startDate));
        if (endDate != null)   spec = spec.and(hasEndDate(endDate));

        return spec;
    }

    public static Specification<Scan> forDailyUsageReport(
            List<FlagType> flags,
            UUID locationId,
            Instant startDate,
            Instant endDate
    ) {
        Specification<Scan> spec = Specification.where(hasFlagsIn(flags));

        if (locationId != null) spec = spec.and(hasLocationId(locationId));
        if (startDate != null)  spec = spec.and(hasStartDate(startDate));
        if (endDate != null)    spec = spec.and(hasEndDate(endDate));

        return spec;
    }

    public static Specification<Scan> forMultipleScansReport(
            List<FlagType> flags,
            UUID locationId,
            Instant startDate,
            Instant endDate
    ) {
        Specification<Scan> spec = Specification.where(hasFlagsIn(flags));

        if (locationId != null) spec = spec.and(hasLocationId(locationId));
        if (startDate != null)  spec = spec.and(hasStartDate(startDate));
        if (endDate != null)    spec = spec.and(hasEndDate(endDate));

        return spec;
    }

    public static Specification<Scan> forReaderReport(
            UUID readerId,
            List<FlagType> flags,
            Instant startDate,
            Instant endDate
    ) {
        Specification<Scan> spec = Specification.where(hasFlagsIn(flags));

        if (readerId != null)   spec = spec.and(hasReaderId(readerId));
        if (startDate != null)  spec = spec.and(hasStartDate(startDate));
        if (endDate != null)    spec = spec.and(hasEndDate(endDate));

        return spec;
    }

    public static Specification<Scan> forGeneralSummaryReport(
            UUID locationId,
            List<FlagType> flags,
            Instant startDate,
            Instant endDate
    ) {
        Specification<Scan> spec = Specification.where(hasFlagsIn(flags));

        if (locationId != null) spec = spec.and(hasLocationId(locationId));
        if (startDate != null)  spec = spec.and(hasStartDate(startDate));
        if (endDate != null)    spec = spec.and(hasEndDate(endDate));

        return spec;
    }
}
