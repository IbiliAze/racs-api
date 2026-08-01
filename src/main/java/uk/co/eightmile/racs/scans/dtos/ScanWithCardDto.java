package uk.co.eightmile.racs.scans.dtos;

import uk.co.eightmile.racs.cards.dtos.CardDto;
import uk.co.eightmile.racs.scans.FlagType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ScanWithCardDto {
    private UUID id;
    private UUID readerId;
    private FlagType flag;
    private CardDto card;
    private String scannedValue;
    private Instant createdAt;
    private Instant updatedAt;
}
