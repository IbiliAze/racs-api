package uk.co.eightmile.racs.scans.dtos;

import uk.co.eightmile.racs.scans.FlagType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ScanDto {
    private UUID id;
    private UUID readerId;
    private FlagType flag;
    private UUID cardId;
    private String scannedValue;
    private Instant createdAt;
    private Instant updatedAt;
}