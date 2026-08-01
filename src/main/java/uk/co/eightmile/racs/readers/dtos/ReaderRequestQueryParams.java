package uk.co.eightmile.racs.readers.dtos;

import uk.co.eightmile.racs.common.dtos.RequestQueryParams;
import uk.co.eightmile.racs.common.validation.SortBy;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class ReaderRequestQueryParams extends RequestQueryParams {

    @SortBy(allowed = {"createdAt", "updatedAt", "username"})
    private String sortBy = "createdAt:desc";

    private String username = "";

    private Boolean inactive;

    private UUID locationId;
}
