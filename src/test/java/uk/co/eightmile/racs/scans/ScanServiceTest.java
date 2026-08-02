package uk.co.eightmile.racs.scans;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.co.eightmile.racs.auth.AuthContext;
import uk.co.eightmile.racs.cards.Card;
import uk.co.eightmile.racs.cards.CardRepository;
import uk.co.eightmile.racs.cards.dtos.CardDto;
import uk.co.eightmile.racs.cards.exceptions.CardNotFoundException;
import uk.co.eightmile.racs.readers.Reader;
import uk.co.eightmile.racs.readers.ReaderRepository;
import uk.co.eightmile.racs.readers.exceptions.ReaderNotFoundException;
import uk.co.eightmile.racs.scans.dtos.*;
import uk.co.eightmile.racs.scans.exceptions.AlreadyPassedException;
import uk.co.eightmile.racs.scans.exceptions.ScanNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScanServiceTest {

    @Mock
    private ScanRepository scanRepository;
    @Mock
    private ScanMapper scanMapper;
    @Mock
    private AuthContext authContext;
    @Mock
    private ReaderRepository readerRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private ApplicationEventPublisher notificationPublisher;
    @InjectMocks
    private ScanService scanService;

    @Test
    void getScans() {
        // Arrange
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

        // Act
        var response = scanService.getScans(queryParams);

        // Assert
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
    void getScansWithCard(){
        // Arrange
        var readerId = UUID.randomUUID();

        var queryParams = new ScanRequestQueryParams();
        queryParams.setPage(2);
        queryParams.setSize(5);
        queryParams.setSortBy("scannedValue:asc");

        var scan = Scan.builder()
                .id(UUID.randomUUID())
                .flag(FlagType.PASSED_OK)
                .scannedValue("ABC123")
                .build();

        var card = Card.builder()
                .id(UUID.randomUUID())
                .value(scan.getScannedValue())
                .label("Label 1")
                .build();

        scan.setCard(card);

        CardDto cardDto = new CardDto();
        cardDto.setId(card.getId());

        ScanWithCardDto scanWithCardDto = new ScanWithCardDto();
        scanWithCardDto.setId(scan.getId());
        scanWithCardDto.setFlag(scan.getFlag());
        scanWithCardDto.setScannedValue(scan.getScannedValue());
        scanWithCardDto.setCard(cardDto);

        when(authContext.getUserId()).thenReturn(readerId);
        when(scanRepository.findAll(ArgumentMatchers.<Specification<Scan>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(scan), PageRequest.of(1, 5), 6));
        when(scanMapper.toScanWithCardDto(scan)).thenReturn(scanWithCardDto);

        // Act
        var response = scanService.getScansWithCard(queryParams);

        // Assert
        assertThat(queryParams.getReaderId()).isEqualTo(readerId);
        assertThat(response.getScansWithCard()).containsExactly(scanWithCardDto);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getTotalItems()).isEqualTo(6);
        assertThat(response.getMessage()).isEqualTo("Scans fetched successfully");

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(authContext).getUserId();
        verify(scanMapper).toScanWithCardDto(scan);
        verify(scanRepository).findAll(ArgumentMatchers.<Specification<Scan>>any(), pageableCaptor.capture());

        var pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "scannedValue"));
    }

    @Test
    void getScansWithCardWhenNoAuthenticatedReader() {
        // Arrange
        var queryParams = new ScanRequestQueryParams();
        queryParams.setPage(1);
        queryParams.setSize(5);

        when(authContext.getUserId()).thenReturn(null);
        when(scanRepository.findAll(ArgumentMatchers.<Specification<Scan>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 5), 0));

        // Act
        var response = scanService.getScansWithCard(queryParams);

        // Assert
        assertThat(queryParams.getReaderId()).isNull();
        assertThat(response.getScansWithCard()).isEmpty();
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getTotalItems()).isZero();

        verify(scanMapper, never()).toScanWithCardDto(any());
    }

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
    void getScanByIdThrowsWhenNotFound() {
        // Arrange
        var scanId = UUID.randomUUID();

        when(scanRepository.findById(scanId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> scanService.getScanById(scanId))
                .isInstanceOf(ScanNotFoundException.class)
                .hasMessage("Scan not found");

        verifyNoInteractions(scanMapper);
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

    @Test
    void getScanByCardValueThrowsWhenNotFound() {
        // Arrange
        var scannedValue = "123";

        when(scanRepository.findByScannedValue(scannedValue)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> scanService.getScanByCardValue(scannedValue))
                .isInstanceOf(ScanNotFoundException.class)
                .hasMessage("Scan not found");

        verifyNoInteractions(scanMapper);
    }

    @Test
    void createScan() {
        // Arrange
        var readerId = UUID.randomUUID();

        var request = new CreateScanRequest();
        request.setReaderId(readerId);
        request.setFlag(FlagType.PASSED_OK);
        request.setScannedValue("123");

        var reader = new Reader();
        reader.setId(readerId);

        var scan = Scan.builder()
                .flag(FlagType.PASSED_OK)
                .scannedValue("123")
                .build();

        var savedScan = Scan.builder()
                .id(UUID.randomUUID())
                .reader(reader)
                .flag(FlagType.PASSED_OK)
                .scannedValue("123")
                .build();

        var scanDto = new ScanDto();
        scanDto.setId(savedScan.getId());
        scanDto.setFlag(savedScan.getFlag());
        scanDto.setScannedValue(savedScan.getScannedValue());

        when(scanMapper.toEntity(request)).thenReturn(scan);
        when(readerRepository.findById(readerId)).thenReturn(Optional.of(reader));
        when(scanRepository.existsByCardValueAndFlag("123", FlagType.PASSED_OK))
                .thenReturn(false);
        when(scanRepository.saveAndFlush(scan)).thenReturn(savedScan);
        when(scanMapper.toDto(savedScan)).thenReturn(scanDto);

        // Act
        var response = scanService.createScan(request);

        // Assert
        assertThat(response.getScan()).isEqualTo(scanDto);
        assertThat(response.getMessage()).isEqualTo("Scan created successfully");
        assertThat(scan.getReader()).isSameAs(reader);
        assertThat(reader.getScans()).contains(scan);

        verify(scanRepository).saveAndFlush(scan);
        verify(notificationPublisher)
                .publishEvent(new ScanUpdate(scanDto, ScanAction.created));
    }

    @Test
    void createScanWithCard() {
        // Arrange
        var readerId = UUID.randomUUID();
        var cardId = UUID.randomUUID();

        var request = new CreateScanRequest();
        request.setReaderId(readerId);
        request.setCardId(cardId);
        request.setFlag(FlagType.PASSED_OK);
        request.setScannedValue("123");

        var reader = new Reader();
        reader.setId(readerId);

        var card = Card.builder()
                .id(cardId)
                .value("123")
                .label("Label 1")
                .build();

        var scan = Scan.builder()
                .flag(FlagType.PASSED_OK)
                .scannedValue("123")
                .build();

        var savedScan = Scan.builder()
                .id(UUID.randomUUID())
                .reader(reader)
                .card(card)
                .flag(FlagType.PASSED_OK)
                .scannedValue("123")
                .build();

        var scanDto = new ScanDto();
        scanDto.setId(savedScan.getId());
        scanDto.setFlag(savedScan.getFlag());
        scanDto.setScannedValue(savedScan.getScannedValue());

        when(scanMapper.toEntity(request)).thenReturn(scan);
        when(readerRepository.findById(readerId)).thenReturn(Optional.of(reader));
        when(scanRepository.existsByCardValueAndFlag("123", FlagType.PASSED_OK))
                .thenReturn(false);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(scanRepository.saveAndFlush(scan)).thenReturn(savedScan);
        when(scanMapper.toDto(savedScan)).thenReturn(scanDto);

        // Act
        var response = scanService.createScan(request);

        // Assert
        assertThat(response.getScan()).isEqualTo(scanDto);
        assertThat(response.getMessage()).isEqualTo("Scan created successfully");
        assertThat(scan.getCard()).isSameAs(card);

        verify(cardRepository).findById(cardId);
        verify(scanRepository).saveAndFlush(scan);
        verify(notificationPublisher)
                .publishEvent(new ScanUpdate(scanDto, ScanAction.created));
    }

    @Test
    void createScanThrowsWhenReaderNotFound() {
        // Arrange
        var readerId = UUID.randomUUID();

        var request = new CreateScanRequest();
        request.setReaderId(readerId);
        request.setFlag(FlagType.PASSED_OK);
        request.setScannedValue("123");

        var scan = Scan.builder()
                .flag(FlagType.PASSED_OK)
                .scannedValue("123")
                .build();

        when(scanMapper.toEntity(request)).thenReturn(scan);
        when(readerRepository.findById(readerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> scanService.createScan(request))
                .isInstanceOf(ReaderNotFoundException.class)
                .hasMessage("Reader not found");

        verify(scanRepository, never()).saveAndFlush(any());
        verifyNoInteractions(cardRepository, notificationPublisher);
    }

    @Test
    void createScanThrowsWhenCardAlreadyPassed() {
        // Arrange
        var readerId = UUID.randomUUID();

        var request = new CreateScanRequest();
        request.setReaderId(readerId);
        request.setFlag(FlagType.PASSED_OK);
        request.setScannedValue("123");

        var reader = new Reader();
        reader.setId(readerId);

        var scan = Scan.builder()
                .flag(FlagType.PASSED_OK)
                .scannedValue("123")
                .build();

        when(scanMapper.toEntity(request)).thenReturn(scan);
        when(readerRepository.findById(readerId)).thenReturn(Optional.of(reader));
        when(scanRepository.existsByCardValueAndFlag("123", FlagType.PASSED_OK))
                .thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> scanService.createScan(request))
                .isInstanceOf(AlreadyPassedException.class)
                .hasMessage("Code already has passed");

        // The duplicate attempt is still recorded, but flagged and never announced
        assertThat(scan.getFlag()).isEqualTo(FlagType.DUPLICATE_ATTEMPT_SERVER);

        verify(scanRepository).save(scan);
        verify(scanRepository, never()).saveAndFlush(any());
        verify(scanMapper, never()).toDto(any());
        verifyNoInteractions(cardRepository, notificationPublisher);
    }

    @Test
    void createScanThrowsWhenCardNotFound() {
        // Arrange
        var readerId = UUID.randomUUID();
        var cardId = UUID.randomUUID();

        var request = new CreateScanRequest();
        request.setReaderId(readerId);
        request.setCardId(cardId);
        request.setFlag(FlagType.REJECTED);
        request.setScannedValue("123");

        var reader = new Reader();
        reader.setId(readerId);

        var scan = Scan.builder()
                .flag(FlagType.REJECTED)
                .scannedValue("123")
                .build();

        when(scanMapper.toEntity(request)).thenReturn(scan);
        when(readerRepository.findById(readerId)).thenReturn(Optional.of(reader));
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> scanService.createScan(request))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found");

        assertThat(scan.getCard()).isNull();

        verify(scanRepository, never()).saveAndFlush(any());
        verifyNoInteractions(notificationPublisher);
    }

    @Test
    void updateScan() {
        // Arrange
        var scanId = UUID.randomUUID();

        var request = new UpdateScanRequest();
        request.setFlag(FlagType.PASSED_OK);

        var scan = Scan.builder()
                .flag(FlagType.PASSED_OK)
                .scannedValue("123")
                .build();

        var scanDto = new ScanDto();
        scanDto.setId(scan.getId());
        scanDto.setFlag(scan.getFlag());
        scanDto.setScannedValue(scan.getScannedValue());

        when(scanRepository.findById(scanId)).thenReturn(Optional.of(scan));
        when(scanMapper.toDto(scan)).thenReturn(scanDto);

        // Act
        var response = scanService.updateScan(scanId, request);

        // Assert
        assertThat(response.getScan()).isEqualTo(scanDto);
        assertThat(response.getMessage()).isEqualTo("Scan updated successfully");

        verify(scanRepository).save(scan);
        verify(scanMapper).update(request, scan);
        verify(notificationPublisher)
                .publishEvent(new ScanUpdate(scanDto, ScanAction.updated));

    }

    @Test
    void updateScanThrowsWhenNotFound() {
        // Arrange
        var scanId = UUID.randomUUID();

        var request = new UpdateScanRequest();
        request.setFlag(FlagType.PASSED_OK);

        when(scanRepository.findById(scanId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> scanService.updateScan(scanId, request))
                .isInstanceOf(ScanNotFoundException.class)
                .hasMessage("Scan not found");

        verify(scanRepository, never()).save(any());
        verifyNoInteractions(scanMapper, notificationPublisher);
    }

    @Test
    void deleteScan() {
        // Arrange
        var scanId = UUID.randomUUID();

        var scan = Scan.builder()
                .id(scanId)
                .flag(FlagType.PASSED_OK)
                .scannedValue("123")
                .build();

        var scanDto = new ScanDto();
        scanDto.setId(scan.getId());
        scanDto.setFlag(scan.getFlag());
        scanDto.setScannedValue(scan.getScannedValue());

        when(scanRepository.findById(scanId)).thenReturn(Optional.of(scan));
        when(scanMapper.toDto(scan)).thenReturn(scanDto);

        // Act
        var response = scanService.deleteScan(scanId);

        // Assert
        assertThat(response.getScan()).isEqualTo(scanDto);
        assertThat(response.getMessage()).isEqualTo("Scan deleted successfully");

        verify(scanRepository).findById(scanId);
        verify(scanRepository).delete(scan);
        verify(scanMapper).toDto(scan);
        verify(notificationPublisher)
                .publishEvent(new ScanUpdate(scanDto, ScanAction.deleted));
    }

    @Test
    void deleteScanThrowsWhenNotFound() {
        // Arrange
        var scanId = UUID.randomUUID();

        when(scanRepository.findById(scanId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> scanService.deleteScan(scanId))
                .isInstanceOf(ScanNotFoundException.class)
                .hasMessage("Scan not found");

        verify(scanRepository, never()).delete(any(Scan.class));
        verifyNoInteractions(scanMapper, notificationPublisher);
    }
}