package uk.co.eightmile.racs.cards.dtos;

import uk.co.eightmile.racs.common.dtos.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CardsAddedResponse extends Response {
    private List<CardDto> cards;
}