package uk.co.eightmile.racs.roles;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import uk.co.eightmile.racs.common.builders.QueryBuilder;
import uk.co.eightmile.racs.permissions.Authority;
import uk.co.eightmile.racs.permissions.Permission;
import uk.co.eightmile.racs.permissions.PermissionRepository;
import uk.co.eightmile.racs.roles.dtos.GetRolesResponse;
import uk.co.eightmile.racs.roles.dtos.RoleDto;
import uk.co.eightmile.racs.roles.dtos.RoleRequestQueryParams;
import uk.co.eightmile.racs.roles.exceptions.RoleNotFoundException;
import uk.co.eightmile.racs.roles.specifications.RoleSpec;

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

        var roleDto = new RoleDto();
        roleDto.setId(roleId);
        roleDto.setName(role.getName());

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        // Act
        var permissions = roleService.getRolePermissions(roleId);

        // Assert
        assertThat(permissions).containsExactly(permission);
    }
}
