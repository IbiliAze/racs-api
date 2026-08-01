package uk.co.eightmile.racs.locations.dtos;

import uk.co.eightmile.racs.common.dtos.RequestQueryParams;
import uk.co.eightmile.racs.common.validation.SortBy;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LocationRequestQueryParams extends RequestQueryParams {

    @SortBy(allowed = {"createdAt", "updatedAt", "name"})
    private String sortBy = "createdAt:desc";

    private String name = "";

    private Boolean inactive;
}
