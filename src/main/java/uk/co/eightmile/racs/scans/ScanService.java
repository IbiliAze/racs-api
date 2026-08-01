package uk.co.eightmile.racs.scans;

import uk.co.eightmile.racs.auth.AuthContext;
import uk.co.eightmile.racs.cards.CardRepository;
import uk.co.eightmile.racs.cards.exceptions.CardNotFoundException;
import uk.co.eightmile.racs.common.builders.QueryBuilder;
import uk.co.eightmile.racs.readers.ReaderRepository;
import uk.co.eightmile.racs.readers.exceptions.ReaderNotFoundException;
import uk.co.eightmile.racs.scans.dtos.*;
import uk.co.eightmile.racs.scans.exceptions.AlreadyPassedException;
import uk.co.eightmile.racs.scans.exceptions.ScanNotFoundException;
import uk.co.eightmile.racs.scans.specifications.ScanSpec;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Service
@Transactional
public class ScanService {
    private final ScanRepository scanRepository;
    private final ScanMapper scanMapper;
    private final ReaderRepository readerRepository;
    private final CardRepository cardRepository;
    private final AuthContext authContext;
    private final ApplicationEventPublisher notificationPublisher;

    public GetScansResponse getScans(ScanRequestQueryParams queryParams) {
        QueryBuilder<Scan> queryBuilder = new QueryBuilder<>(queryParams);
        Specification<Scan> spec = ScanSpec.fromQueryParams(queryParams, false);
        PageRequest pageRequest = queryBuilder.getPageRequest();

        Page<Scan> page = scanRepository.findAll(spec, pageRequest);

        GetScansResponse response = new GetScansResponse();
        response.setScans(page.getContent().stream().map(scanMapper::toDto).toList());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setCurrentPage(page.getNumber() + 1);
        response.setMessage("Scans fetched successfully");

        return response;
    }

    public GetScansResponse getScansWithCard(ScanRequestQueryParams queryParams) {
        UUID readerId = authContext.getUserId();
        if (readerId != null) queryParams.setReaderId(readerId);

        QueryBuilder<Scan> queryBuilder = new QueryBuilder<>(queryParams);
        Specification<Scan> spec = ScanSpec.fromQueryParams(queryParams, true);
        PageRequest pageRequest = queryBuilder.getPageRequest();

        Page<Scan> page = scanRepository.findAll(spec, pageRequest);

        GetScansResponse response = new GetScansResponse();
        response.setScansWithCard(page.getContent().stream().map(scanMapper::toScanWithCardDto).toList());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setCurrentPage(page.getNumber() + 1);
        response.setMessage("Scans fetched successfully");

        return response;
    }

    public SingleItemResponse getScanById(UUID id) {
        var scan = scanRepository.findById(id).orElseThrow(ScanNotFoundException::new);

        var response = new SingleItemResponse();
        response.setScan(scanMapper.toDto(scan));
        response.setMessage("Scan fetched successfully");

        return response;
    }

    public SingleItemResponse getScanByCardValue(String scannedValue) {
        var scan = scanRepository.findByScannedValue(scannedValue).orElseThrow(ScanNotFoundException::new);

        var response = new SingleItemResponse();
        response.setScan(scanMapper.toDto(scan));
        response.setMessage("Scan fetched successfully");

        return response;
    }

    public SingleItemResponse createScan(CreateScanRequest request) {
        var scan = scanMapper.toEntity(request);

        var reader = readerRepository.findById(request.getReaderId()).orElseThrow(ReaderNotFoundException::new);
        reader.addScan(scan);

        if (passedScan(scan.getFlag())) {
            var passed = scanRepository.existsByCardValueAndFlag(request.getScannedValue(), scan.getFlag());

            if (passed) {
                scan.setFlag(FlagType.DUPLICATE_ATTEMPT_SERVER);
                scanRepository.save(scan);
                throw new AlreadyPassedException();
            }
        }

        if (request.getCardId() != null) {
            var card = cardRepository.findById(request.getCardId()).orElseThrow(CardNotFoundException::new);
            scan.setCard(card);
        }

        var savedScan = scanRepository.saveAndFlush(scan);
        var scanDto = scanMapper.toDto(savedScan);

        notificationPublisher.publishEvent(new ScanUpdate(scanDto, ScanAction.created));

        var response = new SingleItemResponse();
        response.setScan(scanDto);
        response.setMessage("Scan created successfully");

        return response;
    }

    public SingleItemResponse updateScan(UUID id, UpdateScanRequest request) {
        var scan = scanRepository.findById(id).orElseThrow(ScanNotFoundException::new);

        scanMapper.update(request, scan);
        scanRepository.save(scan);

        var scanDto = scanMapper.toDto(scan);

        notificationPublisher.publishEvent(new ScanUpdate(scanDto, ScanAction.updated));

        var response = new SingleItemResponse();
        response.setScan(scanDto);
        response.setMessage("Scan updated successfully");

        return response;
    }

    public SingleItemResponse deleteScan(UUID id) {
        var scan = scanRepository.findById(id)
                .orElseThrow(ScanNotFoundException::new);

        scanRepository.delete(scan);

        var scanDto = scanMapper.toDto(scan);

        notificationPublisher.publishEvent(new ScanUpdate(scanDto, ScanAction.deleted));

        var response = new SingleItemResponse();
        response.setScan(scanDto);
        response.setMessage("Scan deleted successfully");

        return response;
    }

    private Boolean passedScan(FlagType flagType) {
        return Set.of(FlagType.PASSED_OK, FlagType.SIMILARITY_CHECK).contains(flagType);
    }
}
