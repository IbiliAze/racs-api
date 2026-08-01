package uk.co.eightmile.racs.roles.dtos;

import uk.co.eightmile.racs.permissions.Authority;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRolePermissionsRequest {
    @NotNull(message = "Permission IDs are required")
    @NotEmpty(message = "At least one permission ID is required")
    private List<Authority> permissionIds;
}
