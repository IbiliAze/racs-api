package uk.co.eightmile.racs.reports.dtos;

import uk.co.eightmile.racs.reports.Status;
import lombok.Data;

import java.time.Instant;

@Data
public class SerialNumberReportRequest {
    private Instant startDate;
    private Instant endDate;
    private String cardValue;
    private Status status;
    private Boolean pdf;
    private int size;
}
