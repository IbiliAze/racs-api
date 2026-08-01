package uk.co.eightmile.racs.cards.dtos;

import uk.co.eightmile.racs.common.dtos.GetAllResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetCardsResponse extends GetAllResponse {
    private List<CardDto> cards;
}