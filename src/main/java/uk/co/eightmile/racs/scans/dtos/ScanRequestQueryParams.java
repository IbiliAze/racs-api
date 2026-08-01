package uk.co.eightmile.racs.scans.dtos;

import uk.co.eightmile.racs.common.dtos.RequestQueryParams;
import uk.co.eightmile.racs.common.validation.SortBy;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class ScanRequestQueryParams extends RequestQueryParams {

    @SortBy(allowed = {"createdAt", "updatedAt", "scannedValue"})
    private String sortBy = "createdAt:desc";

    private String scannedValue = "";

    private String flag = "";

    private UUID readerId;
}
