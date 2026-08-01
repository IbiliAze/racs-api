package uk.co.eightmile.racs.locations;

import uk.co.eightmile.racs.common.dtos.ErrorDto;
import uk.co.eightmile.racs.locations.dtos.*;
import uk.co.eightmile.racs.locations.exceptions.LocationExistsException;
import uk.co.eightmile.racs.locations.exceptions.LocationNotFoundException;
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
@RequestMapping("/api/location")
@Tag(name = "Locations")
public class LocationController {
    private final LocationService locationService;

    @GetMapping
    @Operation(summary = "Gets all locations.")
    public GetLocationsResponse getLocations(@Valid @ModelAttribute LocationRequestQueryParams queryParams) {
        return locationService.getLocations(queryParams);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets a location by ID.")
    public SingleItemResponse getLocationById(
            @Parameter(description = "The ID of the location.")
            @PathVariable(name = "id") UUID id) {
        return locationService.getLocationById(id);
    }

    @PostMapping
    @Operation(summary = "Creates or registers a new location.")
    public ResponseEntity<SingleItemResponse> createLocation(
            @Valid @RequestBody CreateLocationRequest request,
            UriComponentsBuilder uriBuilder) {
        var response = locationService.createLocation(request);
        var uri = uriBuilder.path(("/api/location/{id}"))
                .buildAndExpand(response.getLocation().getId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a location.")
    public SingleItemResponse updateLocation(
            @Parameter(description = "The ID of the location.")
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody UpdateLocationRequest request) {
        return locationService.updateLocation(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a location.")
    public SingleItemResponse deleteLocation(
            @Parameter(description = "The ID of the location.")
            @PathVariable(name = "id") UUID id) {
        return locationService.deleteLocation(id);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorDto> handleLocationNotFound(LocationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorDto(ex.getMessage())
        );
    }

    @ExceptionHandler(LocationExistsException.class)
    public ResponseEntity<ErrorDto> handleLocationExists(LocationExistsException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorDto(ex.getMessage())
        );
    }
}
