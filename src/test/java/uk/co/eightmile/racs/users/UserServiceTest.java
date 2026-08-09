package uk.co.eightmile.racs.users;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.eightmile.racs.auth.AuthService;
import uk.co.eightmile.racs.auth.AuthenticationService;
import uk.co.eightmile.racs.auth.Jwt;
import uk.co.eightmile.racs.auth.JwtService;
import uk.co.eightmile.racs.auth.LoginType;
import uk.co.eightmile.racs.auth.config.JwtConfig;
import uk.co.eightmile.racs.auth.dtos.JwtPrincipalDto;
import uk.co.eightmile.racs.common.exceptions.UnauthorizedException;
import uk.co.eightmile.racs.campaigns.Campaign;
import uk.co.eightmile.racs.campaigns.CampaignRepository;
import uk.co.eightmile.racs.campaigns.exceptions.CampaignNotFoundException;
import uk.co.eightmile.racs.permissions.Authority;
import uk.co.eightmile.racs.permissions.Permission;
import uk.co.eightmile.racs.roles.Role;
import uk.co.eightmile.racs.roles.RoleMapper;
import uk.co.eightmile.racs.roles.RoleRepository;
import uk.co.eightmile.racs.roles.dtos.RoleDto;
import uk.co.eightmile.racs.roles.exceptions.RoleNotFoundException;
import uk.co.eightmile.racs.users.dtos.CreateUserRequest;
import uk.co.eightmile.racs.users.dtos.UpdateUserRolesRequest;
import uk.co.eightmile.racs.users.dtos.UserDto;
import uk.co.eightmile.racs.users.dtos.UserLoginRequest;
import uk.co.eightmile.racs.users.dtos.UserRequestQueryParams;
import uk.co.eightmile.racs.users.exceptions.UserExistsException;
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

    @Test
    void createUser() {
        // Arrange
        var request = new CreateUserRequest();
        request.setFirstName("first-name");
        request.setLastName("last-name");
        request.setEmail("user@example.com");
        request.setPassword("password");
        request.setInactive(false);

        var user = buildUser(UUID.randomUUID());
        var userDto = buildUserDto(user.getId());

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        var response = userService.createUser(request);

        // Assert
        assertThat(response.getUser()).isSameAs(userDto);
        assertThat(response.getMessage()).isEqualTo("User created successfully");
        assertThat(user.getPassword()).isEqualTo("encoded-password");

        verify(userRepository).save(user);
        verifyNoInteractions(campaignRepository);
    }

    @Test
    void createUserAssignsCampaign() {
        // Arrange
        var request = new CreateUserRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");
        request.setCampaignId("campaign-1");

        var campaign = new Campaign();
        campaign.setId("campaign-1");

        var user = buildUser(UUID.randomUUID());
        var userDto = buildUserDto(user.getId());

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(campaignRepository.findById("campaign-1")).thenReturn(Optional.of(campaign));
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        var response = userService.createUser(request);

        // Assert
        assertThat(response.getUser()).isSameAs(userDto);
        assertThat(user.getCampaign()).isSameAs(campaign);

        verify(userRepository).save(user);
    }

    @Test
    void createUserThrowsWhenEmailExists() {
        // Arrange
        var request = new CreateUserRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        var user = buildUser(UUID.randomUUID());

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserExistsException.class)
                .hasMessage("User already exists");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUserThrowsWhenCampaignNotFound() {
        // Arrange
        var request = new CreateUserRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");
        request.setCampaignId("campaign-1");

        var user = buildUser(UUID.randomUUID());

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(campaignRepository.findById("campaign-1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(CampaignNotFoundException.class)
                .hasMessage("Campaign not found");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerUser() {
        // Arrange
        var request = new CreateUserRequest();
        request.setFirstName("first-name");
        request.setLastName("last-name");
        request.setEmail("user@example.com");
        request.setPassword("password");
        request.setInactive(false);

        var user = buildUser(UUID.randomUUID());
        var userDto = buildUserDto(user.getId());

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        var response = userService.registerUser(request);

        // Assert
        assertThat(response.getUser()).isSameAs(userDto);
        assertThat(response.getMessage()).isEqualTo("User created successfully");
        assertThat(user.getPassword()).isEqualTo("encoded-password");

        verify(userRepository).save(user);
    }

    @Test
    void registerUserIgnoresCampaignId() {
        // Arrange
        var request = new CreateUserRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");
        request.setCampaignId("campaign-1");

        var user = buildUser(UUID.randomUUID());
        var userDto = buildUserDto(user.getId());

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        var response = userService.registerUser(request);

        // Assert
        assertThat(response.getUser()).isSameAs(userDto);
        assertThat(user.getCampaign()).isNull();

        verifyNoInteractions(campaignRepository);
    }

    @Test
    void registerUserThrowsWhenEmailExists() {
        // Arrange
        var request = new CreateUserRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        var user = buildUser(UUID.randomUUID());

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(UserExistsException.class)
                .hasMessage("User already exists");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    private UserLoginRequest buildLoginRequest() {
        var request = new UserLoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        return request;
    }

    private UserDetails buildUserDetails() {
        return new org.springframework.security.core.userdetails.User(
                "user@example.com", "encoded-password", List.of());
    }

    @Test
    void login() {
        // Arrange
        var request = buildLoginRequest();
        var httpResponse = mock(HttpServletResponse.class);

        var user = buildUser(UUID.randomUUID());
        var jwtPrincipal = JwtPrincipalDto.builder().id(user.getId()).build();

        var accessToken = mock(Jwt.class);
        var refreshToken = mock(Jwt.class);

        when(authenticationService.loadUserByUsernameAndType("user@example.com", LoginType.USER))
                .thenReturn(buildUserDetails());
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(userRepository.findUserWithRolesAndPermissionsByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(userMapper.toJwtPrincipal(user)).thenReturn(jwtPrincipal);
        when(jwtService.generateAccessToken(jwtPrincipal)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(jwtPrincipal)).thenReturn(refreshToken);
        when(accessToken.toString()).thenReturn("access-token");
        when(refreshToken.toString()).thenReturn("refresh-token");
        when(jwtConfig.getRefreshTokenExpiration()).thenReturn(604800);
        when(jwtConfig.isRefreshCookieSecure()).thenReturn(true);

        // Act
        var response = userService.login(request, httpResponse);

        // Assert
        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getMessage()).isEqualTo("Logged in successfully");

        var cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpResponse).addCookie(cookieCaptor.capture());

        var cookie = cookieCaptor.getValue();
        assertThat(cookie.getName()).isEqualTo("refreshToken");
        assertThat(cookie.getValue()).isEqualTo("refresh-token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/api/user/token/refresh");
        assertThat(cookie.getMaxAge()).isEqualTo(604800);
        assertThat(cookie.getSecure()).isTrue();
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() {
        // Arrange
        var request = buildLoginRequest();
        var httpResponse = mock(HttpServletResponse.class);

        when(authenticationService.loadUserByUsernameAndType("user@example.com", LoginType.USER))
                .thenReturn(buildUserDetails());
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.login(request, httpResponse))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthorized");

        verifyNoInteractions(jwtService);
        verifyNoInteractions(httpResponse);
    }

    @Test
    void loginThrowsWhenUserNotFound() {
        // Arrange
        var request = buildLoginRequest();
        var httpResponse = mock(HttpServletResponse.class);

        when(authenticationService.loadUserByUsernameAndType("user@example.com", LoginType.USER))
                .thenReturn(buildUserDetails());
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(userRepository.findUserWithRolesAndPermissionsByEmail("user@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.login(request, httpResponse))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthorized");

        verifyNoInteractions(jwtService);
        verifyNoInteractions(httpResponse);
    }

    @Test
    void loginThrowsWhenUserInactive() {
        // Arrange
        var request = buildLoginRequest();
        var httpResponse = mock(HttpServletResponse.class);

        var user = buildUser(UUID.randomUUID());
        user.setInactive(true);

        when(authenticationService.loadUserByUsernameAndType("user@example.com", LoginType.USER))
                .thenReturn(buildUserDetails());
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(userRepository.findUserWithRolesAndPermissionsByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> userService.login(request, httpResponse))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthorized");

        verifyNoInteractions(jwtService);
        verifyNoInteractions(httpResponse);
    }

    @Test
    void refreshToken() {
        // Arrange
        var userId = UUID.randomUUID();
        var user = buildUser(userId);
        var jwtPrincipal = JwtPrincipalDto.builder().id(userId).build();

        var refreshToken = mock(Jwt.class);
        var accessToken = mock(Jwt.class);

        when(jwtService.parseToken("refresh-token")).thenReturn(refreshToken);
        when(refreshToken.isExpired()).thenReturn(false);
        when(refreshToken.getPrincipalId()).thenReturn(userId);
        when(userRepository.findUserWithRolesAndPermissions(userId)).thenReturn(Optional.of(user));
        when(userMapper.toJwtPrincipal(user)).thenReturn(jwtPrincipal);
        when(jwtService.generateAccessToken(jwtPrincipal)).thenReturn(accessToken);
        when(accessToken.toString()).thenReturn("access-token");

        // Act
        var response = userService.refreshToken("refresh-token");

        // Assert
        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getMessage()).isEqualTo("Token refreshed successfully");
    }

    @Test
    void refreshTokenThrowsWhenTokenIsInvalid() {
        // Arrange
        when(jwtService.parseToken("invalid-token")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> userService.refreshToken("invalid-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthorized");

        verifyNoInteractions(userRepository);
    }

    @Test
    void refreshTokenThrowsWhenTokenIsExpired() {
        // Arrange
        var refreshToken = mock(Jwt.class);

        when(jwtService.parseToken("expired-token")).thenReturn(refreshToken);
        when(refreshToken.isExpired()).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.refreshToken("expired-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthorized");

        verifyNoInteractions(userRepository);
    }

    @Test
    void refreshTokenThrowsWhenUserNotFound() {
        // Arrange
        var userId = UUID.randomUUID();
        var refreshToken = mock(Jwt.class);

        when(jwtService.parseToken("refresh-token")).thenReturn(refreshToken);
        when(refreshToken.isExpired()).thenReturn(false);
        when(refreshToken.getPrincipalId()).thenReturn(userId);
        when(userRepository.findUserWithRolesAndPermissions(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.refreshToken("refresh-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthorized");

        verify(jwtService, never()).generateAccessToken(any());
    }

    @Test
    void refreshTokenThrowsWhenUserInactive() {
        // Arrange
        var userId = UUID.randomUUID();
        var user = buildUser(userId);
        user.setInactive(true);

        var refreshToken = mock(Jwt.class);

        when(jwtService.parseToken("refresh-token")).thenReturn(refreshToken);
        when(refreshToken.isExpired()).thenReturn(false);
        when(refreshToken.getPrincipalId()).thenReturn(userId);
        when(userRepository.findUserWithRolesAndPermissions(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> userService.refreshToken("refresh-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Unauthorized");

        verify(jwtService, never()).generateAccessToken(any());
        verifyNoInteractions(userMapper);
    }

    @Test
    void updateRoles() {
        // Arrange
        var userId = UUID.randomUUID();

        var existingRole = Role.builder().id(UUID.randomUUID()).name("old-role").build();
        var newRole = Role.builder().id(UUID.randomUUID()).name("new-role").build();

        var user = buildUser(userId);
        user.setRoles(new LinkedHashSet<>(Set.of(existingRole)));

        var request = UpdateUserRolesRequest.builder()
                .roleIds(List.of(newRole.getId()))
                .build();

        var userDto = buildUserDto(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(request.getRoleIds())).thenReturn(List.of(newRole));
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        var response = userService.updateRoles(userId, request);

        // Assert
        assertThat(response.getUser()).isSameAs(userDto);
        assertThat(response.getMessage()).isEqualTo("1 roles updated successfully");
        assertThat(user.getRoles()).containsExactly(newRole);

        verify(userRepository).save(user);
    }

    @Test
    void updateRolesThrowsWhenUserNotFound() {
        // Arrange
        var userId = UUID.randomUUID();

        var request = UpdateUserRolesRequest.builder()
                .roleIds(List.of(UUID.randomUUID()))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateRoles(userId, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verifyNoInteractions(roleRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    void updateRolesThrowsWhenRoleNotFound() {
        // Arrange
        var userId = UUID.randomUUID();

        var role = Role.builder().id(UUID.randomUUID()).name("role-1").build();

        var user = buildUser(userId);

        var request = UpdateUserRolesRequest.builder()
                .roleIds(List.of(role.getId(), UUID.randomUUID()))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findAllById(request.getRoleIds())).thenReturn(List.of(role));

        // Act & Assert
        assertThatThrownBy(() -> userService.updateRoles(userId, request))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role not found");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(userMapper);
    }
}
