package uk.co.eightmile.racs.campaigns.dtos;

import lombok.Data;

import java.time.Instant;

@Data
public class CampaignDto {
    private String id;
    private String name;
    private Instant validFrom;
    private Instant validUntil;
    private Instant createdAt;
    private Instant updatedAt;
}
