package uk.co.eightmile.racs.roles;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import uk.co.eightmile.racs.permissions.Authority;
import uk.co.eightmile.racs.permissions.Permission;
import uk.co.eightmile.racs.permissions.PermissionRepository;
import uk.co.eightmile.racs.permissions.exceptions.PermissionNotFoundException;
import uk.co.eightmile.racs.roles.dtos.CreateRoleRequest;
import uk.co.eightmile.racs.roles.dtos.RoleDto;
import uk.co.eightmile.racs.roles.dtos.RoleRequestQueryParams;
import uk.co.eightmile.racs.roles.dtos.UpdateRolePermissionsRequest;
import uk.co.eightmile.racs.roles.dtos.UpdateRoleRequest;
import uk.co.eightmile.racs.roles.exceptions.RoleNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private PermissionRepository permissionRepository;
    @InjectMocks
    private RoleService roleService;

    @Test
    void getRoles() {
        // Arrange
        var queryParams = new RoleRequestQueryParams();
        queryParams.setPage(0);
        queryParams.setSize(5);
        queryParams.setSortBy("createdAt:asc");
        queryParams.setName("role-1");

        var role = Role.builder()
                .id(UUID.randomUUID())
                .name("role-1")
                .build();

        var roleDto = new RoleDto();
        roleDto.setId(role.getId());
        roleDto.setName(role.getName());

        when(roleRepository
                .findAll(ArgumentMatchers.<Specification<Role>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(role), PageRequest.of(0, 5), 5));
        when(roleMapper.toDto(role)).thenReturn(roleDto);

        // Act
        var response = roleService.getRoles(queryParams);

        // Assert
        assertThat(response.getRoles()).containsExactly(roleDto);
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getTotalItems()).isEqualTo(5);
        assertThat(response.getMessage()).isEqualTo("Roles fetched successfully");

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(roleRepository).
                findAll(ArgumentMatchers.<Specification<Role>>any(), pageableCaptor.capture());

        var pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "createdAt"));
    }

    @Test
    void getRoleById() {
        // Arrange
        var roleId = UUID.randomUUID();

        var role = Role.builder()
                .id(roleId)
                .name("role-1")
                .build();

        var roleDto = new RoleDto();
        roleDto.setId(roleId);
        roleDto.setName(role.getName());

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleMapper.toDto(role)).thenReturn(roleDto);

        // Act
        var response = roleService.getRoleById(roleId);

        // Assert
        assertThat(response.getRole()).isSameAs(roleDto);
        assertThat(response.getMessage()).isEqualTo("Role fetched successfully");

        verify(roleRepository).findById(roleId);
    }

    @Test
    void getRoleByIdThrowsWhenNotFound() {
        // Arrange
        var roleId = UUID.randomUUID();

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.getRoleById(roleId))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found");

        verifyNoInteractions(roleMapper);
    }

    @Test
    void getRolePermissions() {
        // Arrange
        var roleId = UUID.randomUUID();

        var permission = Permission.builder()
                .id(Authority.ADMIN).build();

        var role = Role.builder()
                .id(roleId)
                .name("role-1")
                .permissions(Set.of(permission))
                .build();

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        // Act
        var permissions = roleService.getRolePermissions(roleId);

        // Assert
        assertThat(permissions).containsExactly(permission);

        verify(roleRepository).findById(roleId);
    }

    @Test
    void getRolePermissionsThrowsWhenNotFound() {
        // Arrange
        var roleId = UUID.randomUUID();

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.getRolePermissions(roleId))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found");
    }

    @Test
    void createRole() {
        // Arrange
        var request = new CreateRoleRequest();
        request.setName("role-1");
        request.setDescription("description");

        var role = Role.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        var roleDto = new RoleDto();
        roleDto.setId(role.getId());
        roleDto.setName(role.getName());

        when(roleMapper.toEntity(request)).thenReturn(role);
        when(roleMapper.toDto(role)).thenReturn(roleDto);

        // Act
        var response = roleService.createRole(request);

        // Assert
        assertThat(response.getRole()).isSameAs(roleDto);
        assertThat(response.getMessage()).isEqualTo("Role created successfully");

        verify(roleRepository).save(role);
    }

    @Test
    void updatePermissions() {
        // Arrange
        var roleId = UUID.randomUUID();

        var existingPermission = Permission.builder().id(Authority.ADMIN).build();
        var newPermission = Permission.builder().id(Authority.READER).build();

        var role = Role.builder()
                .id(roleId)
                .name("role-1")
                .permissions(new HashSet<>(Set.of(existingPermission)))
                .build();

        var request = new UpdateRolePermissionsRequest();
        request.setPermissionIds(List.of(Authority.READER));

        var roleDto = new RoleDto();
        roleDto.setId(roleId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(request.getPermissionIds())).thenReturn(List.of(newPermission));
        when(roleMapper.toDto(role)).thenReturn(roleDto);

        // Act
        var response = roleService.updatePermissions(roleId, request);

        // Assert
        assertThat(response).isSameAs(roleDto);
        assertThat(role.getPermissions()).containsExactly(newPermission);

        verify(roleRepository).save(role);
    }

    @Test
    void updatePermissionsThrowsWhenPermissionNotFound() {
        // Arrange
        var roleId = UUID.randomUUID();

        var role = Role.builder()
                .id(roleId)
                .name("role-1")
                .permissions(new HashSet<>())
                .build();

        var request = new UpdateRolePermissionsRequest();
        request.setPermissionIds(List.of(Authority.ADMIN, Authority.READER));

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findAllById(request.getPermissionIds()))
                .thenReturn(List.of(Permission.builder().id(Authority.ADMIN).build()));

        // Act & Assert
        assertThatThrownBy(() -> roleService.updatePermissions(roleId, request))
                .isInstanceOf(PermissionNotFoundException.class)
                .hasMessage("Permission not found");

        verify(roleRepository, never()).save(any());
        verifyNoInteractions(roleMapper);
    }

    @Test
    void updatePermissionsThrowsWhenRoleNotFound() {
        // Arrange
        var roleId = UUID.randomUUID();

        var request = new UpdateRolePermissionsRequest();
        request.setPermissionIds(List.of(Authority.ADMIN));

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.updatePermissions(roleId, request))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found");

        verifyNoInteractions(permissionRepository);
        verifyNoInteractions(roleMapper);
    }

    @Test
    void removePermission() {
        // Arrange
        var roleId = UUID.randomUUID();
        var permission = Permission.builder().id(Authority.ADMIN).build();

        var role = Role.builder()
                .id(roleId)
                .name("role-1")
                .permissions(new HashSet<>(Set.of(permission)))
                .build();

        var roleDto = new RoleDto();
        roleDto.setId(roleId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(Authority.ADMIN)).thenReturn(Optional.of(permission));
        when(roleMapper.toDto(role)).thenReturn(roleDto);

        // Act
        var response = roleService.removePermission(roleId, Authority.ADMIN);

        // Assert
        assertThat(response).isSameAs(roleDto);
        assertThat(role.getPermissions()).isEmpty();

        verify(roleRepository).save(role);
    }

    @Test
    void removePermissionThrowsWhenRoleNotFound() {
        // Arrange
        var roleId = UUID.randomUUID();

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.removePermission(roleId, Authority.ADMIN))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found");

        verifyNoInteractions(permissionRepository);
        verifyNoInteractions(roleMapper);
    }

    @Test
    void removePermissionThrowsWhenPermissionNotFound() {
        // Arrange
        var roleId = UUID.randomUUID();

        var role = Role.builder()
                .id(roleId)
                .name("role-1")
                .permissions(new HashSet<>())
                .build();

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(Authority.ADMIN)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.removePermission(roleId, Authority.ADMIN))
                .isInstanceOf(PermissionNotFoundException.class)
                .hasMessage("Permission not found");

        verify(roleRepository, never()).save(any());
        verifyNoInteractions(roleMapper);
    }

    @Test
    void removePermissionThrowsWhenPermissionNotOnRole() {
        // Arrange
        var roleId = UUID.randomUUID();
        var permission = Permission.builder().id(Authority.ADMIN).build();

        var role = Role.builder()
                .id(roleId)
                .name("role-1")
                .permissions(new HashSet<>())
                .build();

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findById(Authority.ADMIN)).thenReturn(Optional.of(permission));

        // Act & Assert
        assertThatThrownBy(() -> roleService.removePermission(roleId, Authority.ADMIN))
                .isInstanceOf(PermissionNotFoundException.class)
                .hasMessage("Permission not found");

        verify(roleRepository, never()).save(any());
        verifyNoInteractions(roleMapper);
    }

    @Test
    void updateRole() {
        // Arrange
        var roleId = UUID.randomUUID();

        var request = new UpdateRoleRequest();
        request.setName("new name");
        request.setDescription("new description");

        var role = Role.builder()
                .id(roleId)
                .name("old name")
                .description("old description")
                .build();

        var roleDto = new RoleDto();
        roleDto.setId(roleId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleMapper.toDto(role)).thenReturn(roleDto);

        // Act
        var response = roleService.updateRole(roleId, request);

        // Assert
        assertThat(response.getRole()).isSameAs(roleDto);
        assertThat(response.getMessage()).isEqualTo("Role updated successfully");

        verify(roleMapper).update(request, role);
        verify(roleRepository).save(role);
    }

    @Test
    void updateRoleThrowsWhenNotFound() {
        // Arrange
        var roleId = UUID.randomUUID();

        var request = new UpdateRoleRequest();
        request.setName("new name");

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.updateRole(roleId, request))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found");

        verify(roleRepository, never()).save(any());
        verifyNoInteractions(roleMapper);
    }

    @Test
    void deleteRole() {
        // Arrange
        var roleId = UUID.randomUUID();

        var role = Role.builder()
                .id(roleId)
                .name("role-1")
                .build();

        var roleDto = new RoleDto();
        roleDto.setId(roleId);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleMapper.toDto(role)).thenReturn(roleDto);

        // Act
        var response = roleService.deleteRole(roleId);

        // Assert
        assertThat(response.getRole()).isSameAs(roleDto);
        assertThat(response.getMessage()).isEqualTo("Role deleted successfully");

        verify(roleRepository).findById(roleId);
        verify(roleRepository).deleteById(roleId);
    }

    @Test
    void deleteRoleThrowsWhenNotFound() {
        // Arrange
        var roleId = UUID.randomUUID();

        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> roleService.deleteRole(roleId))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found");

        verify(roleRepository, never()).deleteById(any());
        verifyNoInteractions(roleMapper);
    }
}
