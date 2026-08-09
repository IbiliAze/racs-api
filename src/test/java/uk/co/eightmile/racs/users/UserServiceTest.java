package uk.co.eightmile.racs.users;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import uk.co.eightmile.racs.auth.AuthService;
import uk.co.eightmile.racs.auth.AuthenticationService;
import uk.co.eightmile.racs.auth.JwtService;
import uk.co.eightmile.racs.auth.config.JwtConfig;
import uk.co.eightmile.racs.campaigns.CampaignRepository;
import uk.co.eightmile.racs.roles.RoleMapper;
import uk.co.eightmile.racs.roles.RoleRepository;

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
}
