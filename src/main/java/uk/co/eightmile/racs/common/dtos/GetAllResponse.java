package uk.co.eightmile.racs.common.dtos;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetAllResponse extends Response {
    private int currentPage;
    private int totalPages;
    private long totalItems;
}
