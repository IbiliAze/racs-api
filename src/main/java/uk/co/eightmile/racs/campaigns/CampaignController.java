package uk.co.eightmile.racs.campaigns;

import uk.co.eightmile.racs.common.dtos.ErrorDto;
import uk.co.eightmile.racs.campaigns.dtos.*;
import uk.co.eightmile.racs.campaigns.exceptions.CampaignExistsException;
import uk.co.eightmile.racs.campaigns.exceptions.CampaignNotFoundException;
import uk.co.eightmile.racs.locations.dtos.LocationDto;
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

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/campaign")
@Tag(name = "Campaigns")
public class CampaignController {
    private final CampaignService campaignService;

    @GetMapping
    @Operation(summary = "Gets all campaigns.")
    public GetCampaignsResponse getCampaigns(@Valid @ModelAttribute CampaignRequestQueryParams queryParams) {
        return campaignService.getCampaigns(queryParams);
    }

    @GetMapping("/count")
    @Operation(summary = "Gets the number of campaigns.")
    public CampaignCountResponse getCampaignCount(
            @Parameter(description = "Optional filter to count only campaigns whose name contains this value.")
            @RequestParam(name = "name", required = false) String name) {
        return campaignService.getCampaignCount(name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets an campaign by ID.")
    public SingleItemResponse getCampaignById(
            @Parameter(description = "The ID of the campaign.")
            @PathVariable(name = "id") String id) {
        return campaignService.getCampaignById(id);
    }

    @GetMapping("/{id}/locations")
    @Operation(summary = "Get campaign locations")
    public List<LocationDto> getCampaignLocations(
            @Parameter(description = "The ID of the campaign.")
            @PathVariable(name = "id") String id) {
        return campaignService.getCampaignLocations(id);
    }

    @PostMapping
    @Operation(summary = "Creates or registers a new campaign.")
    public ResponseEntity<SingleItemResponse> createCampaign(
            @Valid @RequestBody CreateCampaignRequest request,
            UriComponentsBuilder uriBuilder) {
        var response = campaignService.createCampaign(request);
        var uri = uriBuilder.path(("/api/campaign/{id}"))
                .buildAndExpand(response.getCampaign().getId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates an campaign.")
    public SingleItemResponse updateCampaign(
            @Parameter(description = "The ID of the campaign.")
            @PathVariable(name = "id") String id,
            @Valid @RequestBody UpdateCampaignRequest request) {
        return campaignService.updateCampaign(id, request);
    }

    @PutMapping("/{id}/locations")
    @Operation(summary = "Updates campaign locations.")
    public SingleItemResponse updateLocations(
            @Parameter(description = "The ID of the campaign.")
            @PathVariable(name = "id") String id,
            @Valid @RequestBody UpdateCampaignLocationsRequest request) {
        return campaignService.updateLocations(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes an campaign.")
    public SingleItemResponse deleteCampaign(
            @Parameter(description = "The ID of the campaign.")
            @PathVariable(name = "id") String id) {
        return campaignService.deleteCampaign(id);
    }

    @ExceptionHandler(CampaignNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCampaignNotFound(CampaignNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorDto(ex.getMessage())
        );
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorDto> handleLocationNotFound(LocationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorDto(ex.getMessage())
        );
    }

    @ExceptionHandler(CampaignExistsException.class)
    public ResponseEntity<ErrorDto> handleCampaignExists(CampaignExistsException ex) {
        return ResponseEntity.badRequest().body(
                new ErrorDto(ex.getMessage())
        );
    }
}
