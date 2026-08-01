package uk.co.eightmile.racs.cards.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class CreateCardRequest {
    @NotBlank(message = "Campaign ID is required")
    private String campaignId;

    @NotBlank(message = "Card value is required")
    @Size(max = 255, message = "Card value must be less than 255 characters")
    private String value;

    @NotBlank(message = "Card label is required")
    @Size(max = 255, message = "Card label must be less than 255 characters")
    private String label;

    @NotBlank(message = "Card type is required")
    private String type;

    private Map<String, Object> metadata;
}
