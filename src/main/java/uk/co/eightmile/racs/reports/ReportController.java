package uk.co.eightmile.racs.reports;

import uk.co.eightmile.racs.reports.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/report")
@Tag(name = "Reports")
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/general-summary")
    @Operation(summary = "Get general summary report.")
    public GeneralSummaryReportDto getGeneralSummaryReport(
            @Valid @RequestBody GeneralSummaryReportRequest request
    ) {
        return reportService.getGeneralSummaryReport(request);
    }

    @PostMapping("/daily-usage")
    @Operation(summary = "Get daily usage report.")
    public List<DailyUsageReportDto> getDailyUsageReport(
            @Valid @RequestBody DailyUsageReportRequest request
    ) {
        return reportService.getDailyUsageReport(request);
    }

    @PostMapping("/location")
    @Operation(summary = "Get location report.")
    public List<LocationReportDto> getLocationReport(
            @Valid @RequestBody LocationReportRequest request
    ) {
        return reportService.getLocationReport(request);
    }

    @PostMapping("/multiple-scans")
    @Operation(summary = "Get multiple scans report.")
    public List<MultipleScanReportDto> getMultipleScanReport(
            @Valid @RequestBody MultipleScanReportRequest request
    ) {
        return reportService.getMultipleScanReport(request);
    }

    @PostMapping("/reader")
    @Operation(summary = "Get reader report.")
    public List<ReaderReportDto> getReaderReport(
            @Valid @RequestBody ReaderReportRequest request
    ) {
        return reportService.getReaderReport(request);
    }

    @PostMapping("/serial-number")
    public ResponseEntity<Page<SerialNumberReportDto>> getSerialNumberReport(
            @RequestBody SerialNumberReportRequest request,
            @PageableDefault(size = 2147483647, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(reportService.getSerialNumberReport(request, pageable));
    }
}