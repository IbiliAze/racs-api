package uk.co.eightmile.racs.campaigns.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateCampaignLocationsRequest {
    @NotNull(message = "Location IDs are required")
    @NotEmpty(message = "At least one location ID is required")
    private List<UUID> locationIds;
}
