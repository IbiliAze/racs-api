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
import uk.co.eightmile.racs.roles.RoleMapper;
import uk.co.eightmile.racs.roles.RoleRepository;
import uk.co.eightmile.racs.users.dtos.UserDto;
import uk.co.eightmile.racs.users.dtos.UserRequestQueryParams;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
}
