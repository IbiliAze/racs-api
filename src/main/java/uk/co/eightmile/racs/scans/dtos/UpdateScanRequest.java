package uk.co.eightmile.racs.scans.dtos;

import uk.co.eightmile.racs.scans.FlagType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateScanRequest {
    @NotNull(message = "Flag is required")
    private FlagType flag;
}