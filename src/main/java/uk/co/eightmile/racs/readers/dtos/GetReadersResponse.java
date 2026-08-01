package uk.co.eightmile.racs.readers.dtos;

import uk.co.eightmile.racs.common.dtos.GetAllResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetReadersResponse extends GetAllResponse {
    private List<ReaderDto> readers;
}
