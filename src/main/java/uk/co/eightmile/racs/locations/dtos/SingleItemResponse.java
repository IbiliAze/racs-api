package uk.co.eightmile.racs.locations.dtos;

import uk.co.eightmile.racs.common.dtos.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SingleItemResponse extends Response {
    private LocationDto location;
}
