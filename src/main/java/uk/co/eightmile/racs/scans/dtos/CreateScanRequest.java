package uk.co.eightmile.racs.scans.dtos;

import uk.co.eightmile.racs.scans.FlagType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateScanRequest {
    @NotNull(message = "Reader ID is required")
    private UUID readerId;

    @NotNull(message = "Flag is required")
    private FlagType flag;

    private UUID cardId;

    @NotBlank(message = "Scanned value is required")
    @Size(max = 255, message = "Scanned value must be less than 255 characters")
    private String scannedValue;
}
