package uk.co.eightmile.racs.locations.dtos;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class LocationDto {
    private UUID id;
    private String name;
    private boolean inactive;
    private Instant createdAt;
    private Instant updatedAt;
}
