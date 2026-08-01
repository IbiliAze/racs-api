package uk.co.eightmile.racs.campaigns.dtos;

import uk.co.eightmile.racs.common.dtos.GetAllResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetCampaignsResponse extends GetAllResponse {
    private List<CampaignDto> campaigns;
}
