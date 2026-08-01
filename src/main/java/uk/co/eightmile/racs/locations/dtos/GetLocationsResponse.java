package uk.co.eightmile.racs.locations.dtos;

import uk.co.eightmile.racs.common.dtos.GetAllResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetLocationsResponse extends GetAllResponse {
    private List<LocationDto> locations;
}
