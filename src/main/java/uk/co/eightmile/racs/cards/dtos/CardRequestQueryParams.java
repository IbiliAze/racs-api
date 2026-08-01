package uk.co.eightmile.racs.cards.dtos;

import uk.co.eightmile.racs.common.dtos.RequestQueryParams;
import uk.co.eightmile.racs.common.validation.SortBy;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class CardRequestQueryParams extends RequestQueryParams {

    @SortBy(allowed = {"createdAt", "updatedAt", "label", "value", "type"})
    private String sortBy = "createdAt:desc";

    private String campaignId;

    private String value = "";

    private String type = "";

    private Boolean used;

    private Boolean invalidated;
}
