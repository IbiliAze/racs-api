package uk.co.eightmile.racs.auth;

import uk.co.eightmile.racs.common.exceptions.UnauthorizedException;
import uk.co.eightmile.racs.readers.Reader;
import uk.co.eightmile.racs.readers.ReaderRepository;
import uk.co.eightmile.racs.users.User;
import uk.co.eightmile.racs.users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final ReaderRepository readerRepository;

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UUID getId() {
        var authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }

        var principal = authentication.getPrincipal();

        if (!(principal instanceof UUID id)) {
            throw new UnauthorizedException();
        }

        return id;
    }

    public User getCurrentUser() {
        return userRepository.findById(getId()).orElse(null);
    }

    public Reader getCurrentReader() {
        return readerRepository.findById(getId()).orElse(null);
    }
}
