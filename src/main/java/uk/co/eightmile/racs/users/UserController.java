package uk.co.eightmile.racs.users;

import uk.co.eightmile.racs.common.dtos.ErrorDto;
import uk.co.eightmile.racs.auth.JwtResponse;
import uk.co.eightmile.racs.common.dtos.UpdatePasswordRequest;
import uk.co.eightmile.racs.common.exceptions.UnauthorizedException;
import uk.co.eightmile.racs.permissions.Permission;
import uk.co.eightmile.racs.roles.dtos.RoleDto;
import uk.co.eightmile.racs.roles.exceptions.RoleNotFoundException;
import uk.co.eightmile.racs.users.dtos.*;
import uk.co.eightmile.racs.users.exceptions.UserExistsException;
import uk.co.eightmile.racs.users.exceptions.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/user")
@Tag(name = "Users")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Gets all users.")
    public GetUsersResponse getUsers(@Valid @ModelAttribute UserRequestQueryParams queryParams) {
        return userService.getUsers(queryParams);
    }

    @GetMapping("/auth")
    @Operation(summary = "Get logged in user information.")
    public SingleItemResponse getUserAuth() {
        return userService.getUserAuth();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets a user by ID.")
    public SingleItemResponse getUserById(
            @Parameter(description = "The ID of the user.")
            @PathVariable(name = "id") UUID id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "Get user roles")
    public List<RoleDto> getUserRoles(
            @Parameter(description = "The ID of the user.")
            @PathVariable(name = "id") UUID id) {
        return userService.getUserRoles(id);
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "Get user permissions")
    public List<Permission> getUserPermissions(
            @Parameter(description = "The ID of the user.")
            @PathVariable(name = "id") UUID id) {
        return userService.getUserPermissions(id);
    }

    @PostMapping("/register")
    @Operation(summary = "Registers a new user.")
    public ResponseEntity<SingleItemResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            UriComponentsBuilder uriBuilder) {
        var response = userService.registerUser(request);
        var uri = uriBuilder.path(("/api/user/{id}"))
                .buildAndExpand(response.getUser().getId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping
    @Operation(summary = "Creates a new user.")
    public ResponseEntity<SingleItemResponse> registerUser(
            @Valid @RequestBody CreateUserRequest request,
            UriComponentsBuilder uriBuilder) {
        var response = userService.createUser(request);
        var uri = uriBuilder.path(("/api/user/{id}"))
                .buildAndExpand(response.getUser().getId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping("/token")
    @Operation(summary = "Logs in a user")
    public JwtResponse login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response) {
        return userService.login(request, response);
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "Refreshes the access token.")
    public JwtResponse refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException();
        }
        return userService.refreshToken(refreshToken);
    }

    @PostMapping("/{id}/roles")
    @Operation(summary = "Add roles to a user with role IDs.")
    public SingleItemResponse addRoles(
            @Parameter(description = "The ID of the user")
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody AddRolesToUserRequest request) {
        return userService.addRoles(id, request.getRoleIds());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a user.")
    public SingleItemResponse updateUser(
            @Parameter(description = "The ID of the user.")
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Updates user password.")
    public SingleItemResponse updatePassword(
            @Parameter(description = "The ID of the user.")
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody UpdatePasswordRequest request) {
        return userService.updatePassword(id, request);
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Updates user roles.")
    public SingleItemResponse updateRoles(
            @Parameter(description = "The ID of the user.")
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        return userService.updateRoles(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a user.")
    public SingleItemResponse deleteUser(
            @Parameter(description = "The ID of the user.")
            @PathVariable(name = "id") UUID id) {
        return userService.deleteUser(id);
    }

    @DeleteMapping("/{id}/role/{roleId}")
    @Operation(summary = "Removes role from a user")
    public SingleItemResponse removeRole(
            @Parameter(description = "The ID of the user")
            @PathVariable(name = "id") UUID id,
            @Parameter(description = "The ID of the role")
            @PathVariable(name = "roleId") UUID roleId) {
        return userService.removeRole(id, roleId);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorDto(ex.getMessage())
        );
    }

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ErrorDto> handleUserExists(UserExistsException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorDto(ex.getMessage())
        );
    }
}
