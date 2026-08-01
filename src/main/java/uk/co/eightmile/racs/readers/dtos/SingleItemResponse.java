package uk.co.eightmile.racs.readers.dtos;

import uk.co.eightmile.racs.common.dtos.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SingleItemResponse extends Response {
    private ReaderDto reader;
}
