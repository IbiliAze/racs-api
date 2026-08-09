package uk.co.eightmile.racs.users;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.eightmile.racs.auth.AuthService;
import uk.co.eightmile.racs.auth.AuthenticationService;
import uk.co.eightmile.racs.auth.JwtService;
import uk.co.eightmile.racs.auth.config.JwtConfig;
import uk.co.eightmile.racs.campaigns.CampaignRepository;
import uk.co.eightmile.racs.permissions.Authority;
import uk.co.eightmile.racs.permissions.Permission;
import uk.co.eightmile.racs.roles.Role;
import uk.co.eightmile.racs.roles.RoleMapper;
import uk.co.eightmile.racs.roles.RoleRepository;
import uk.co.eightmile.racs.roles.dtos.RoleDto;
import uk.co.eightmile.racs.users.dtos.UserDto;
import uk.co.eightmile.racs.users.dtos.UserRequestQueryParams;
import uk.co.eightmile.racs.users.exceptions.UserNotFoundException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AuthService authService;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtConfig jwtConfig;
    @Mock
    private CampaignRepository campaignRepository;
    @InjectMocks
    private UserService userService;

    private User buildUser(UUID id) {
        var user = new User();
        user.setId(id);
        user.setFirstName("first-name");
        user.setLastName("last-name");
        user.setEmail("user@example.com");
        user.setPassword("password");

        return user;
    }

    private UserDto buildUserDto(UUID id) {
        var userDto = new UserDto();
        userDto.setId(id);
        userDto.setEmail("user@example.com");

        return userDto;
    }

    @Test
    void getUsers() {
        // Arrange
        var queryParams = new UserRequestQueryParams();
        queryParams.setPage(0);
        queryParams.setSize(5);
        queryParams.setSortBy("email:asc");
        queryParams.setEmail("user@example.com");

        var user = buildUser(UUID.randomUUID());
        var userDto = buildUserDto(user.getId());

        when(userRepository
                .findAll(ArgumentMatchers.<Specification<User>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 5), 5));
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        var response = userService.getUsers(queryParams);

        // Assert
        assertThat(response.getUsers()).containsExactly(userDto);
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getTotalItems()).isEqualTo(5);
        assertThat(response.getMessage()).isEqualTo("Users fetched successfully");

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository)
                .findAll(ArgumentMatchers.<Specification<User>>any(), pageableCaptor.capture());

        var pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "email"));
    }

    @Test
    void getUserById() {
        // Arrange
        var userId = UUID.randomUUID();

        var user = buildUser(userId);
        var userDto = buildUserDto(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        var response = userService.getUserById(userId);

        // Assert
        assertThat(response.getUser()).isSameAs(userDto);
        assertThat(response.getMessage()).isEqualTo("User fetched successfully");

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserByIdThrowsWhenNotFound() {
        // Arrange
        var userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verifyNoInteractions(userMapper);
    }

    @Test
    void getUserAuth() {
        // Arrange
        var userId = UUID.randomUUID();

        var user = buildUser(userId);
        var userDto = buildUserDto(userId);

        when(authService.getCurrentUser()).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        var response = userService.getUserAuth();

        // Assert
        assertThat(response.getUser()).isSameAs(userDto);
        assertThat(response.getMessage()).isEqualTo("User authentication details fetched successfully");
    }

    @Test
    void getUserAuthThrowsWhenNoCurrentUser() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserAuth())
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verifyNoInteractions(userMapper);
    }

    @Test
    void getUserRoles() {
        // Arrange
        var userId = UUID.randomUUID();

        var role = Role.builder()
                .id(UUID.randomUUID())
                .name("role-1")
                .build();

        var user = buildUser(userId);
        user.setRoles(new LinkedHashSet<>(Set.of(role)));

        var roleDto = new RoleDto();
        roleDto.setId(role.getId());
        roleDto.setName(role.getName());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleMapper.toDto(role)).thenReturn(roleDto);

        // Act
        var roles = userService.getUserRoles(userId);

        // Assert
        assertThat(roles).containsExactly(roleDto);

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserRolesThrowsWhenUserNotFound() {
        // Arrange
        var userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserRoles(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verifyNoInteractions(roleMapper);
    }

    @Test
    void getUserPermissions() {
        // Arrange
        var userId = UUID.randomUUID();

        var permission = Permission.builder().id(Authority.ADMIN).build();

        var role = Role.builder()
                .id(UUID.randomUUID())
                .name("role-1")
                .permissions(Set.of(permission))
                .build();

        var user = buildUser(userId);
        user.setRoles(new LinkedHashSet<>(Set.of(role)));

        when(userRepository.findUserWithRolesAndPermissions(userId)).thenReturn(Optional.of(user));

        // Act
        var permissions = userService.getUserPermissions(userId);

        // Assert
        assertThat(permissions).containsExactly(permission);

        verify(userRepository).findUserWithRolesAndPermissions(userId);
    }

    @Test
    void getUserPermissionsThrowsWhenUserNotFound() {
        // Arrange
        var userId = UUID.randomUUID();

        when(userRepository.findUserWithRolesAndPermissions(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserPermissions(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }
}
