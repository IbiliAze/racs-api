package uk.co.eightmile.racs.cards.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateCardsRequest {
    @NotNull
    List<CreateCardRequest> cards;
}
