package uk.co.eightmile.racs.reports.dtos;

import lombok.Data;

@Data
public class GeneralSummaryReportDto {
    private Long cardCount;
    private Long usedCardCount;
    private Long readerCount;
}
