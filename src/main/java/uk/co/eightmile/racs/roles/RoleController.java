package uk.co.eightmile.racs.roles;

import uk.co.eightmile.racs.common.dtos.ErrorDto;
import uk.co.eightmile.racs.permissions.Authority;
import uk.co.eightmile.racs.permissions.Permission;
import uk.co.eightmile.racs.roles.dtos.*;
import uk.co.eightmile.racs.roles.exceptions.RoleNotFoundException;
import uk.co.eightmile.racs.roles.dtos.SingleItemResponse;
import uk.co.eightmile.racs.roles.dtos.RoleRequestQueryParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/role")
@Tag(name = "Roles")
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Get all roles.")
    public GetRolesResponse getRoles(@Valid @ModelAttribute RoleRequestQueryParams queryParams) {
        return roleService.getRoles(queryParams);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID.")
    public SingleItemResponse getRoleById(
            @Parameter(description = "The ID of the role.")
            @PathVariable UUID id) {
        return roleService.getRoleById(id);
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "Get role permissions.")
    public List<Permission> getRolePermissions(
            @Parameter(description = "The ID of the role.")
            @PathVariable UUID id) {
        return roleService.getRolePermissions(id);
    }

    @PostMapping
    @Operation(summary = "Creates a new role.")
    public SingleItemResponse createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        return roleService.createRole(request);
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "Updates permissions for a role.")
    public RoleDto updatePermissions(
            @Parameter(description = "The ID of the role.")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRolePermissionsRequest request) {
        return roleService.updatePermissions(id, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a role by ID.")
    public SingleItemResponse updateRole(
            @Parameter(description = "The ID of the role")
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return roleService.updateRole(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a role by ID.")
    public SingleItemResponse deleteRole(
            @Parameter(description = "The ID of the role")
            @PathVariable UUID id) {
        return roleService.deleteRole(id);
    }

    @DeleteMapping("/{id}/permission/{permissionId}")
    @Operation(summary = "Remove a permission from role.")
    public RoleDto removePermission(
            @Parameter(description = "The ID of the role")
            @PathVariable UUID id,
            @Parameter(description = "The ID of the permission")
            @PathVariable Authority permissionId) {
        return roleService.removePermission(id, permissionId);
    }


    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorDto> handleRoleNotFound(RoleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorDto(ex.getMessage())
        );
    }
}
