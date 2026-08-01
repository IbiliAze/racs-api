package uk.co.eightmile.racs.cards.dtos;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class CardDto {
    private UUID id;
    private String value;
    private String campaignId;
    private String label;
    private String type;
    private Instant validFrom;
    private Instant validUntil;
    private Map<String, Object> metadata;
    private boolean used;
    private Instant usedAt;
    private boolean invalidated;
    private Instant createdAt;
    private Instant updatedAt;
}
