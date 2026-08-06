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
import uk.co.eightmile.racs.permissions.PermissionRepository;
import uk.co.eightmile.racs.roles.dtos.GetRolesResponse;
import uk.co.eightmile.racs.roles.dtos.RoleRequestQueryParams;
import uk.co.eightmile.racs.roles.specifications.RoleSpec;

import java.util.List;
import java.util.Optional;
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
                .name("location-1")
                .inactive(false)
                .build();

        var locationDto = new LocationDto();
        locationDto.setId(location.getId());
        locationDto.setName(location.getName());
        locationDto.setInactive(location.isInactive());

        when(locationRepository
                .findAll(ArgumentMatchers.<Specification<Location>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(location), PageRequest.of(0, 5), 5));
        when(locationMapper.toDto(location)).thenReturn(locationDto);

        // Act
        var response = locationService.getLocations(queryParams);

        // Assert
        assertThat(response.getLocations()).containsExactly(locationDto);
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getTotalItems()).isEqualTo(5);
        assertThat(response.getMessage()).isEqualTo("Locations fetched successfully");

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(locationRepository).
                findAll(ArgumentMatchers.<Specification<Location>>any(), pageableCaptor.capture());

        var pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "createdAt"));
    }
}
