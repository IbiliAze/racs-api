package uk.co.eightmile.racs.cards.dtos;

import uk.co.eightmile.racs.common.dtos.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SingleItemResponse extends Response {
    private CardDto card;
}