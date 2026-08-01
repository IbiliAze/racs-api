package uk.co.eightmile.racs.reports.dtos;

import lombok.Data;

import java.time.Instant;

@Data
public class MultipleScanReportDto {
    private String cardValue;
    private Instant firstUsedAt;
    private Instant attemptedAt;
    private String locationName;
    private String readerUsername;
}
