package uk.co.eightmile.racs.roles.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest  {
    @NotBlank(message = "Role name is required")
    @Size(max = 255, message = "Role name must be less than 255 characters")
    private String name;

    private String description;
}
