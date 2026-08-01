package uk.co.eightmile.racs.reports.dtos;

import lombok.Data;

@Data
public class ReaderReportDto {
    private String scannerUsername;
    private String locationName;
    private Long usedCards;
}
