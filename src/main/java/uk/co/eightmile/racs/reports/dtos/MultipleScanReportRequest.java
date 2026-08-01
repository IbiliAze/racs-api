package uk.co.eightmile.racs.reports.dtos;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class MultipleScanReportRequest {
    private Instant startDate;
    private Instant endDate;
    private UUID locationId;
}
