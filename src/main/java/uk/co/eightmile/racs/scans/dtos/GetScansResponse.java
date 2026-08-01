package uk.co.eightmile.racs.scans.dtos;

import uk.co.eightmile.racs.common.dtos.GetAllResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetScansResponse extends GetAllResponse {
    private List<ScanDto> scans;
    private List<ScanWithCardDto> scansWithCard;
}
