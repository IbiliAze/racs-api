package uk.co.eightmile.racs.scans;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.co.eightmile.racs.scans.dtos.ScanDto;
import uk.co.eightmile.racs.scans.dtos.ScanRequestQueryParams;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanServiceTest {

    @Mock
    private ScanRepository scanRepository;
    @Mock
    private ScanMapper scanMapper;
    @InjectMocks
    private ScanService scanService;

    @Test
    void getScans() {
        var queryParams = new ScanRequestQueryParams();
        queryParams.setPage(2);
        queryParams.setSize(5);
        queryParams.setSortBy("scannedValue:asc");

        var scan = Scan.builder()
                .id(UUID.randomUUID())
                .flag(FlagType.PASSED_OK)
                .scannedValue("ABC123")
                .build();

        var scanDto = new ScanDto();
        scanDto.setId(scan.getId());
        scanDto.setFlag(scan.getFlag());
        scanDto.setScannedValue(scan.getScannedValue());

        when(scanRepository.findAll(ArgumentMatchers.<Specification<Scan>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(scan), PageRequest.of(1, 5), 6));
        when(scanMapper.toDto(scan)).thenReturn(scanDto);

        var response = scanService.getScans(queryParams);

        assertThat(response.getScans()).containsExactly(scanDto);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getTotalItems()).isEqualTo(6);
        assertThat(response.getMessage()).isEqualTo("Scans fetched successfully");

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(scanRepository).findAll(ArgumentMatchers.<Specification<Scan>>any(), pageableCaptor.capture());

        var pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "scannedValue"));
    }

    @Test
    void getScansWithCard(){}

    @Test
    void getScanById(){
        // Arrange
        UUID scanId = UUID.randomUUID();
        var scan = Scan.builder()
                .id(scanId)
                .flag(FlagType.PASSED_OK)
                .scannedValue("ABC123")
                .build();

        var scanDto = new ScanDto();
        scanDto.setId(scan.getId());
        scanDto.setFlag(scan.getFlag());
        scanDto.setScannedValue(scan.getScannedValue());

        when(scanRepository.findById(scanId)).thenReturn(Optional.of(scan));
        when(scanMapper.toDto(scan)).thenReturn(scanDto);

        // Act
        var response = scanService.getScanById(scanId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getScan()).isEqualTo(scanDto);
        assertThat(response.getMessage()).isEqualTo("Scan fetched successfully");

        verify(scanRepository).findById(scanId);
        verify(scanMapper).toDto(scan);
    }

    @Test
    void getScanByCardValue() {
        // Arrange
        String scannedValue = "123";
        var scan = Scan.builder()
                .id(UUID.randomUUID())
                .flag(FlagType.PASSED_OK)
                .scannedValue(scannedValue)
                .build();

        var scanDto = new ScanDto();
        scanDto.setId(scan.getId());
        scanDto.setFlag(scan.getFlag());
        scanDto.setScannedValue(scan.getScannedValue());

        when(scanRepository.findByScannedValue(scannedValue)).thenReturn(Optional.of(scan));
        when(scanMapper.toDto(scan)).thenReturn(scanDto);

        // Act
        var response = scanService.getScanByCardValue(scannedValue);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getScan()).isEqualTo(scanDto);
        assertThat(response.getMessage()).isEqualTo("Scan fetched successfully");

        verify(scanRepository).findByScannedValue(scannedValue);
        verify(scanMapper).toDto(scan);
    }
}