package uk.co.eightmile.racs.readers.dtos;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ReaderDto {
    private UUID id;
    private String username;
    private boolean inactive;
    private UUID locationId;
    private Instant createdAt;
    private Instant updatedAt;
}
