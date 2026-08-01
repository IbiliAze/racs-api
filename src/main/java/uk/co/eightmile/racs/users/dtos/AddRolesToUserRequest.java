package uk.co.eightmile.racs.users.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AddRolesToUserRequest {
    @NotNull(message = "Role IDs are required")
    @NotEmpty(message = "At least one role ID is required")
    private List<UUID> roleIds;
}
