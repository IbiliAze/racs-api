package uk.co.eightmile.racs.reports.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class DailyUsageReportDto {
    private Date date;
    private Long usedCards;
}
