package uk.co.eightmile.racs.common.dtos;

import lombok.*;

@Data
public abstract class RequestQueryParams {
    private String sortBy;
    private int page = 0;
    private int size = 10;
}
