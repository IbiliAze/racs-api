package uk.co.eightmile.racs.campaigns.dtos;

import uk.co.eightmile.racs.common.dtos.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SingleItemResponse extends Response {
    private CampaignDto campaign;
}
