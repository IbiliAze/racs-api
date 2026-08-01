package uk.co.eightmile.racs.readers;

import uk.co.eightmile.racs.auth.*;
import uk.co.eightmile.racs.common.builders.QueryBuilder;
import uk.co.eightmile.racs.common.dtos.UpdatePasswordRequest;
import uk.co.eightmile.racs.common.exceptions.UnauthorizedException;
import uk.co.eightmile.racs.readers.dtos.*;
import uk.co.eightmile.racs.readers.exceptions.ReaderExistsException;
import uk.co.eightmile.racs.readers.exceptions.ReaderNotFoundException;
import uk.co.eightmile.racs.readers.specifications.ReaderSpec;
import uk.co.eightmile.racs.locations.Location;
import uk.co.eightmile.racs.locations.LocationRepository;
import uk.co.eightmile.racs.locations.exceptions.LocationNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class ReaderService {
    private final ReaderRepository readerRepository;
    private final AuthService authService;
    private final JwtService jwtService;
    private final ReaderMapper readerMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;
    private final LocationRepository locationRepository;

    public GetReadersResponse getReaders(ReaderRequestQueryParams queryParams) {
        QueryBuilder<Reader> queryBuilder = new QueryBuilder<>(queryParams);
        Specification<Reader> spec = ReaderSpec.fromQueryParams(queryParams);
        PageRequest pageRequest = queryBuilder.getPageRequest();

        Page<Reader> page = readerRepository.findAll(spec, pageRequest);

        GetReadersResponse response = new GetReadersResponse();
        response.setReaders(page.getContent().stream().map(readerMapper::toDto).toList());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setCurrentPage(page.getNumber() + 1);
        response.setMessage("Readers fetched successfully");

        return response;
    }

    public SingleItemResponse getReaderById(UUID id) {
        var reader = readerRepository.findById(id).orElseThrow(ReaderNotFoundException::new);

        var response = new SingleItemResponse();
        response.setReader(readerMapper.toDto(reader));
        response.setMessage("Reader fetched successfully");

        return response;
    }

    public SingleItemResponse getReaderAuth() {
        var reader = authService.getCurrentReader();
        if (reader == null) throw new ReaderNotFoundException();

        var response = new SingleItemResponse();
        response.setReader(readerMapper.toDto(reader));
        response.setMessage("Reader authentication details fetched successfully");

        return response;
    }

    public SingleItemResponse createReader(CreateReaderRequest request) {
        var reader = readerMapper.toEntity(request);

        if (readerRepository.existsByUsername(request.getUsername())) {
            throw new ReaderExistsException();
        }

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(LocationNotFoundException::new);


        reader.setPassword(passwordEncoder.encode(reader.getPassword()));
        reader.setLocation(location);
        readerRepository.save(reader);

        var response = new SingleItemResponse();
        response.setReader(readerMapper.toDto(reader));
        response.setMessage("Reader created successfully");

        return response;
    }

    public JwtResponse login(ReaderLoginRequest request, HttpServletResponse response) {
        UserDetails userDetails = authenticationService
                .loadUserByUsernameAndType(request.getUsername(), LoginType.READER);

        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new UnauthorizedException();
        }

        var reader = readerRepository.findByUsername(request.getUsername())
                .orElseThrow(UnauthorizedException::new);

        if (reader.isInactive()) {
            throw new UnauthorizedException();
        }

        var jwtPrincipalDto = readerMapper.toJwtPrincipal(reader);
        var accessToken = jwtService.generateReaderAccessToken(jwtPrincipalDto);

        JwtResponse jwtResponse = new JwtResponse(accessToken.toString());
        jwtResponse.setMessage("Logged in successfully");

        return jwtResponse;
    }

    public SingleItemResponse updateReader(UUID id, UpdateReaderRequest request) {
        var reader = readerRepository.findById(id)
                .orElseThrow(ReaderNotFoundException::new);

        if (request.getUsername() != null &&
                !request.getUsername().equalsIgnoreCase(reader.getUsername()) &&
                readerRepository.existsByUsername(request.getUsername())) {
            throw new ReaderExistsException();
        }

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(LocationNotFoundException::new);

        readerMapper.update(request, reader);
        reader.setLocation(location);

        var savedReader = readerRepository.save(reader);

        var response = new SingleItemResponse();
        response.setReader(readerMapper.toDto(savedReader));
        response.setMessage("Reader updated successfully");

        return response;
    }

    public SingleItemResponse updatePassword(UUID id, UpdatePasswordRequest request) {
        var reader = readerRepository.findById(id)
                .orElseThrow(ReaderNotFoundException::new);

        reader.setPassword(passwordEncoder.encode(request.getPassword()));

        readerRepository.save(reader);

        var response = new SingleItemResponse();
        response.setReader(readerMapper.toDto(reader));
        response.setMessage("Reader password updated successfully");

        return response;
    }

    public SingleItemResponse deleteReader(UUID id) {
        var reader = readerRepository.findById(id).orElseThrow(ReaderNotFoundException::new);

        readerRepository.delete(reader);

        var response = new SingleItemResponse();
        response.setReader(readerMapper.toDto(reader));
        response.setMessage("Reader deleted successfully");

        return response;
    }
}
