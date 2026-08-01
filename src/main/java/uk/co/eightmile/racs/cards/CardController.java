package uk.co.eightmile.racs.cards;

import uk.co.eightmile.racs.cards.dtos.*;
import uk.co.eightmile.racs.cards.exceptions.CardExistsException;
import uk.co.eightmile.racs.cards.exceptions.CardNotFoundException;
import uk.co.eightmile.racs.common.dtos.ErrorDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/card")
@Tag(name = "Cards")
public class CardController {
    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Gets all cards.")
    public GetCardsResponse getCards(@Valid @ModelAttribute CardRequestQueryParams queryParams) {
        return cardService.getCards(queryParams);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gets a card by ID.")
    public SingleItemResponse getCardById(
            @Parameter(description = "The ID of the card.")
            @PathVariable(name = "id") UUID id) {
        return cardService.getCardById(id);
    }

    @GetMapping("/value/{value}")
    @Operation(summary = "Gets a card by value.")
    public SingleItemResponse getCardByValue(
            @Parameter(description = "The value of the card.")
            @PathVariable(name = "value") String value) {
        return cardService.getCardByValue(value);
    }

    @PostMapping
    @Operation(summary = "Creates a new card.")
    public ResponseEntity<SingleItemResponse> createCard(
            @Valid @RequestBody CreateCardRequest request,
            UriComponentsBuilder uriBuilder) {
        var response = cardService.createCard(request);
        var uri = uriBuilder.path("/api/card/{id}")
                .buildAndExpand(response.getCard().getId()).toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @PostMapping("/many")
    @Operation(summary = "Creates new cards.")
    public CardsAddedResponse createCards(
            @Valid @RequestBody CreateCardsRequest request) {
        return cardService.createCards(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Updates a card.")
    public SingleItemResponse updateCard(
            @Parameter(description = "The ID of the card.")
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody UpdateCardRequest request) {
        return cardService.updateCard(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletes a card.")
    public SingleItemResponse deleteCard(
            @Parameter(description = "The ID of the card.")
            @PathVariable(name = "id") UUID id) {
        return cardService.deleteCard(id);
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCardNotFound(CardNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler(CardExistsException.class)
    public ResponseEntity<ErrorDto> handleCardExists(CardExistsException ex) {
        log.warn("400: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorDto(ex.getMessage()));
    }
}
