package uk.co.eightmile.racs.scans;

import uk.co.eightmile.racs.cards.exceptions.CardNotFoundException;
import uk.co.eightmile.racs.common.dtos.ErrorDto;
import uk.co.eightmile.racs.scans.dtos.*;
import uk.co.eightmile.racs.scans.exceptions.AlreadyPassedException;
import uk.co.eightmile.racs.scans.exceptions.ScanExistsException;
import uk.co.eightmile.racs.scans.exceptions.ScanNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/scan")
@Tag(name = "Scans")
public class ScanController {
    private final ScanService scanService;

    @GetMapping
    @Operation(summary = "Gets all scans.")
    public GetScansResponse getScans(@Valid @ModelAttribute ScanRequestQueryParams queryParams) {
        return scanService.getScans(queryParams);
    }

    @GetMapping("/with-card")
    @Operation(summary = "Gets all scans with card.")
    public GetScansResponse getScansWithCard(@Valid @ModelAttribute ScanRequestQueryParams queryParams) {
        return scanService.getScansWithCard(queryParams);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets a scan by ID.")
    public SingleItemResponse getScanById(
            @Parameter(description = "The ID of the scan.")
            @PathVariable(name = "id") UUID id) {
        return scanService.getScanById(id);
    }

    @PostMapping
    @Operation(summary = "Creates or registers a new scan.")
    public ResponseEntity<SingleItemResponse> createScan(
            @Valid @RequestBody CreateScanRequest request,
            UriComponentsBuilder uriBuilder) {
        var response = scanService.createScan(request);
        var uri = uriBuilder.path(("/api/scan/{id}"))
                .buildAndExpand(response.getScan().getId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a scan.")
    public SingleItemResponse updateScan(
            @Parameter(description = "The ID of the scan.")
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody UpdateScanRequest request) {
        return scanService.updateScan(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a scan.")
    public SingleItemResponse deleteScan(
            @Parameter(description = "The ID of the scan.")
            @PathVariable(name = "id") UUID id) {
        return scanService.deleteScan(id);
    }

    @ExceptionHandler(ScanNotFoundException.class)
    public ResponseEntity<ErrorDto> handleScanNotFound(ScanNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorDto(ex.getMessage())
        );
    }

    @ExceptionHandler(AlreadyPassedException.class)
    public ResponseEntity<ErrorDto> handleAlreadyPassed(AlreadyPassedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorDto(ex.getMessage())
        );
    }

    @ExceptionHandler(ScanExistsException.class)
    public ResponseEntity<ErrorDto> handleScanExists(ScanExistsException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorDto(ex.getMessage())
        );
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCardNotFound(CardNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorDto(ex.getMessage())
        );
    }
}
