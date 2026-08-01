package uk.co.eightmile.racs.reports.dtos;

import lombok.Data;

@Data
public class LocationReportDto {
    private String locationName;
    private Long totalScanCount;
    private Long usedCardCount;
}
