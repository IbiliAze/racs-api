package uk.co.eightmile.racs.readers;

import uk.co.eightmile.racs.common.dtos.ErrorDto;
import uk.co.eightmile.racs.auth.JwtResponse;
import uk.co.eightmile.racs.common.dtos.UpdatePasswordRequest;
import uk.co.eightmile.racs.readers.dtos.*;
import uk.co.eightmile.racs.readers.exceptions.ReaderExistsException;
import uk.co.eightmile.racs.readers.exceptions.ReaderNotFoundException;
import uk.co.eightmile.racs.locations.exceptions.LocationNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/reader")
@Tag(name = "Readers")
public class ReaderController {
    private final ReaderService readerService;

    @GetMapping
    @Operation(summary = "Gets all readers.")
    public GetReadersResponse getReaders(@Valid @ModelAttribute ReaderRequestQueryParams queryParams) {
        return readerService.getReaders(queryParams);
    }

    @GetMapping("/auth")
    @Operation(summary = "Get logged in reader information.")
    public SingleItemResponse getReaderAuth() {
        return readerService.getReaderAuth();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets a reader by ID.")
    public SingleItemResponse getReaderById(
            @Parameter(description = "The ID of the reader.")
            @PathVariable(name = "id") UUID id) {
        return readerService.getReaderById(id);
    }

    @PostMapping
    @Operation(summary = "Creates or registers a new reader.")
    public ResponseEntity<SingleItemResponse> createReader(
            @Valid @RequestBody CreateReaderRequest request,
            UriComponentsBuilder uriBuilder) {
        var response = readerService.createReader(request);
        var uri = uriBuilder.path(("/api/reader/{id}"))
                .buildAndExpand(response.getReader().getId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping("/token")
    @Operation(summary = "Logs in a reader")
    public JwtResponse login(
            @Valid @RequestBody ReaderLoginRequest request,
            HttpServletResponse response) {
        return readerService.login(request, response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a reader.")
    public SingleItemResponse updateReader(
            @Parameter(description = "The ID of the reader.")
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody UpdateReaderRequest request) {
        return readerService.updateReader(id, request);
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Updates reader password.")
    public SingleItemResponse updatePassword(
            @Parameter(description = "The ID of the reader.")
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody UpdatePasswordRequest request) {
        return readerService.updatePassword(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a reader.")
    public SingleItemResponse deleteReader(
            @Parameter(description = "The ID of the reader.")
            @PathVariable(name = "id") UUID id) {
        return readerService.deleteReader(id);
    }

    @ExceptionHandler(ReaderNotFoundException.class)
    public ResponseEntity<ErrorDto> handleReaderNotFound(ReaderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorDto(ex.getMessage())
        );
    }

    @ExceptionHandler(ReaderExistsException.class)
    public ResponseEntity<ErrorDto> handleReaderExists(ReaderExistsException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorDto(ex.getMessage())
        );
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorDto> handleLocationNotFound(LocationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorDto(ex.getMessage())
        );
    }
}
