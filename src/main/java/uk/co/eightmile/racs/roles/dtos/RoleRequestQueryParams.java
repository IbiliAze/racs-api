package uk.co.eightmile.racs.roles.dtos;

import uk.co.eightmile.racs.common.dtos.RequestQueryParams;
import uk.co.eightmile.racs.common.validation.Lowercase;
import uk.co.eightmile.racs.common.validation.SortBy;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleRequestQueryParams extends RequestQueryParams {

    @SortBy(allowed = {"createdAt", "updatedAt", "name"})
    private String sortBy = "createdAt:desc";

    private String name = "";
}
