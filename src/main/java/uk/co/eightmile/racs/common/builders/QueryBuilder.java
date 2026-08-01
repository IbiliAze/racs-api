package uk.co.eightmile.racs.common.builders;

import uk.co.eightmile.racs.common.dtos.RequestQueryParams;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@Getter
public class QueryBuilder<T> {
    private final PageRequest pageRequest;
    private final Specification<T> spec;

    public QueryBuilder(RequestQueryParams queryParams) {
        String rawSortBy = queryParams.getSortBy();
        String sortField = "createdAt";
        Sort.Direction sortDirection = Sort.Direction.DESC;

        if (rawSortBy != null && !rawSortBy.isBlank()) {
            String[] parts = rawSortBy.split(":");

            sortField = parts[0];

            if (parts.length > 1) {
                sortDirection = parts[1].equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
            }
        }

        this.spec = (root, query, cb) -> cb.conjunction();

        Sort sort = Sort.by(sortDirection, sortField);

        int requestedPage = queryParams.getPage();
        int springPage = Math.max(requestedPage - 1, 0);

        this.pageRequest = PageRequest.of(
                springPage,
                Math.max(queryParams.getSize(), 1),
                sort
        );
    }
}