package uk.co.eightmile.racs.locations.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateLocationRequest {
    @NotBlank(message = "Location name is required")
    @Size(max = 255, message = "Location name must be less than 255 characters")
    private String name;

    @NotNull(message = "Inactive is required")
    private Boolean inactive;
}
