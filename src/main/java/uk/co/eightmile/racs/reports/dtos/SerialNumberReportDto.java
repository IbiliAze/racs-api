package uk.co.eightmile.racs.reports.dtos;

import uk.co.eightmile.racs.reports.Status;
import lombok.Data;

import java.time.Instant;

@Data
public class SerialNumberReportDto {
    private String cardValue;
    private String cardLabel;
    private String cardType;
    private Status status;
    private Instant usedAt;
}
