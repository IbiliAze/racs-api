package uk.co.eightmile.racs.users;

import uk.co.eightmile.racs.auth.*;
import uk.co.eightmile.racs.auth.config.JwtConfig;
import uk.co.eightmile.racs.common.builders.QueryBuilder;
import uk.co.eightmile.racs.common.dtos.UpdatePasswordRequest;
import uk.co.eightmile.racs.common.exceptions.UnauthorizedException;
import uk.co.eightmile.racs.campaigns.CampaignRepository;
import uk.co.eightmile.racs.campaigns.exceptions.CampaignNotFoundException;
import uk.co.eightmile.racs.permissions.Permission;
import uk.co.eightmile.racs.roles.dtos.RoleDto;
import uk.co.eightmile.racs.users.dtos.*;
import uk.co.eightmile.racs.roles.exceptions.RoleNotFoundException;
import uk.co.eightmile.racs.roles.RoleMapper;
import uk.co.eightmile.racs.roles.RoleRepository;
import uk.co.eightmile.racs.users.exceptions.UserExistsException;
import uk.co.eightmile.racs.users.exceptions.UserNotFoundException;
import uk.co.eightmile.racs.users.specifications.UserSpec;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;
    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;
    private final CampaignRepository campaignRepository;

    public GetUsersResponse getUsers(UserRequestQueryParams queryParams) {
        QueryBuilder<User> queryBuilder = new QueryBuilder<>(queryParams);
        Specification<User> spec = UserSpec.fromQueryParams(queryParams);
        PageRequest pageRequest = queryBuilder.getPageRequest();

        Page<User> page = userRepository.findAll(spec, pageRequest);

        GetUsersResponse response = new GetUsersResponse();
        response.setUsers(page.getContent().stream().map(userMapper::toDto).toList());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setCurrentPage(page.getNumber() + 1);
        response.setMessage("Users fetched successfully");

        return response;
    }

    public SingleItemResponse getUserById(UUID id) {
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        var response = new SingleItemResponse();
        response.setUser(userMapper.toDto(user));
        response.setMessage("User fetched successfully");

        return response;
    }

    public SingleItemResponse getUserAuth() {
        var user = authService.getCurrentUser();
        if (user == null) throw new UserNotFoundException();

        var response = new SingleItemResponse();
        response.setUser(userMapper.toDto(user));
        response.setMessage("User authentication details fetched successfully");

        return response;
    }

    public List<RoleDto> getUserRoles(UUID id) {
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        return user.getRoles().stream().map(roleMapper::toDto).toList();
    }

    public List<Permission> getUserPermissions(UUID id) {
        var user = userRepository.findUserWithRolesAndPermissions(id).orElseThrow(UserNotFoundException::new);
        return user.getRoles().stream().flatMap(r -> r.getPermissions().stream()).toList();
    }

    public SingleItemResponse createUser(CreateUserRequest request) {
        var user = userMapper.toEntity(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserExistsException();
        }

        if (request.getCampaignId() != null) {
            var campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(CampaignNotFoundException::new);

            user.setCampaign(campaign);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        var response = new SingleItemResponse();
        response.setUser(userMapper.toDto(user));
        response.setMessage("User created successfully");

        return response;
    }

    public SingleItemResponse registerUser(CreateUserRequest request) {
        var user = userMapper.toEntity(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserExistsException();
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        var response = new SingleItemResponse();
        response.setUser(userMapper.toDto(user));
        response.setMessage("User created successfully");

        return response;
    }

    public JwtResponse login(UserLoginRequest request, HttpServletResponse response) {
        UserDetails userDetails = authenticationService
                .loadUserByUsernameAndType(request.getEmail(), LoginType.USER);

        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new UnauthorizedException();
        }

        var user = userRepository.findUserWithRolesAndPermissionsByEmail(
                request.getEmail()).orElseThrow(UnauthorizedException::new);

        if (user.isInactive()) {
            throw new UnauthorizedException();
        }

        var jwtPrincipalDto = userMapper.toJwtPrincipal(user);
        var accessToken = jwtService.generateAccessToken(jwtPrincipalDto);
        var refreshToken = jwtService.generateRefreshToken(jwtPrincipalDto);

        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/api/user/token/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        cookie.setSecure(jwtConfig.isRefreshCookieSecure());

        response.addCookie(cookie);

        JwtResponse jwtResponse = new JwtResponse(accessToken.toString());
        jwtResponse.setMessage("Logged in successfully");

        return jwtResponse;
    }

    public JwtResponse refreshToken(String refreshToken) {
        var jwt = jwtService.parseToken(refreshToken);
        if (jwt == null || jwt.isExpired()) throw new UnauthorizedException();

        var user = userRepository.findUserWithRolesAndPermissions(
                jwt.getPrincipalId()).orElseThrow(UnauthorizedException::new);

        if (user.isInactive()) {
            throw new UnauthorizedException();
        }

        var jwtPrincipalDto = userMapper.toJwtPrincipal(user);
        var accessToken = jwtService.generateAccessToken(jwtPrincipalDto);

        JwtResponse jwtResponse = new JwtResponse(accessToken.toString());
        jwtResponse.setMessage("Token refreshed successfully");

        return jwtResponse;
    }

    public SingleItemResponse updateRoles(UUID id, UpdateUserRolesRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        var requestedRoleIds = request.getRoleIds();

        var roles = roleRepository.findAllById(requestedRoleIds);

        if (roles.size() != requestedRoleIds.size()) {
            throw new RoleNotFoundException();
        }

        user.getRoles().clear();
        user.getRoles().addAll(roles);

        userRepository.save(user);

        var response = new SingleItemResponse();
        response.setUser(userMapper.toDto(user));
        response.setMessage(requestedRoleIds.size() +  " roles updated successfully");

        return response;
    }

    public SingleItemResponse addRoles(UUID id, List<UUID> roleIds) {
        var user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        for (UUID roleId : roleIds) {
            var role = roleRepository.findById(roleId).orElseThrow(RoleNotFoundException::new);

            user.addRole(role);
        }

        userRepository.save(user);

        var response = new SingleItemResponse();
        response.setUser(userMapper.toDto(user));
        response.setMessage(roleIds.size() +  " roles added successfully");

        return response;
    }

    public SingleItemResponse removeRole(UUID id, UUID roleId) {
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        var role = roleRepository.findById(roleId).orElseThrow(RoleNotFoundException::new);

        user.removeRole(role);
        userRepository.save(user);

        var response = new SingleItemResponse();
        response.setUser(userMapper.toDto(user));
        response.setMessage("User role removed successfully");

        return response;
    }

    public SingleItemResponse updateUser(UUID id, UpdateUserRequest request) {
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        if (request.getEmail() != null &&
                !request.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new UserExistsException();
        }

        if (request.getCampaignId() != null) {
            var campaign = campaignRepository.findById(request.getCampaignId())
                    .orElseThrow(CampaignNotFoundException::new);

            user.setCampaign(campaign);
        }

        userMapper.update(request, user);
        userRepository.save(user);

        var response = new SingleItemResponse();
        response.setUser(userMapper.toDto(user));
        response.setMessage("User updated successfully");

        return response;
    }

    public SingleItemResponse updatePassword(UUID id, UpdatePasswordRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        var response = new SingleItemResponse();
        response.setUser(userMapper.toDto(user));
        response.setMessage("User password updated successfully");

        return response;
    }

    public SingleItemResponse deleteUser(UUID id) {
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        userRepository.delete(user);

        var response = new SingleItemResponse();
        response.setUser(userMapper.toDto(user));
        response.setMessage("User deleted successfully");

        return response;
    }
}
