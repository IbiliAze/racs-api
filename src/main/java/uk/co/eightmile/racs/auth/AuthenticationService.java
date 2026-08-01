package uk.co.eightmile.racs.auth;

import uk.co.eightmile.racs.readers.ReaderRepository;
import uk.co.eightmile.racs.users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@AllArgsConstructor
@Service
public class AuthenticationService implements UserDetailsService {
    private final UserRepository userRepository;
    private final ReaderRepository readerRepository;

    // Keep this for Spring Security's internal use (e.g. JWT filter re-authentication)
    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("Use loadUserByUsernameAndType() instead");
    }

    public UserDetails loadUserByUsernameAndType(String username, LoginType loginType) {
        HasCredentials found = switch (loginType) {
            case USER     -> userRepository.findUserByEmail(username)
                    .map(u -> (HasCredentials) u)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            case READER   -> readerRepository.findByUsername(username)
                    .map(u -> (HasCredentials) u)
                    .orElseThrow(() -> new UsernameNotFoundException("Reader not found"));
        };

        return new User(found.getLoginId(), found.getPassword(), Collections.emptyList());
    }
}