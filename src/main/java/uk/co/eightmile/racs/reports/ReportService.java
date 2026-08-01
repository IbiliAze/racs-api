package uk.co.eightmile.racs.reports;

import uk.co.eightmile.racs.cards.Card;
import uk.co.eightmile.racs.cards.CardRepository;
import uk.co.eightmile.racs.cards.specifications.CardSpec;
import uk.co.eightmile.racs.reports.dtos.*;
import uk.co.eightmile.racs.scans.FlagType;
import uk.co.eightmile.racs.scans.Scan;
import uk.co.eightmile.racs.scans.ScanRepository;
import uk.co.eightmile.racs.scans.specifications.ScanSpec;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ReportService {
    private final CardRepository cardRepository;
    private final ScanRepository scanRepository;
    private final List<FlagType> passedFlags = FlagType.getPassedFlags();
    private final List<FlagType> duplicateFlags = FlagType.getDuplicateFlags();

    public GeneralSummaryReportDto getGeneralSummaryReport(GeneralSummaryReportRequest request) {
        var spec = ScanSpec.forGeneralSummaryReport(
                request.getLocationId(),
                passedFlags,
                request.getStartDate(),
                request.getEndDate()
        );

        var scans = scanRepository.findAll(spec);
        var cardCount = cardRepository.count();

        var dto = new GeneralSummaryReportDto();
        dto.setCardCount(cardCount);
        dto.setUsedCardCount(scans.stream()
                .filter(s -> s.getCard() != null)
                .map(s -> s.getCard().getId())
                .distinct()
                .count());
        dto.setReaderCount(scans.stream()
                .map(s -> s.getReader().getId())
                .distinct()
                .count());
        return dto;
    }

    public List<DailyUsageReportDto> getDailyUsageReport(DailyUsageReportRequest request) {
        var spec = ScanSpec.forDailyUsageReport(
                passedFlags,
                request.getLocationId(),
                request.getStartDate(),
                request.getEndDate()
        );

        return scanRepository.findAll(spec)
                .stream()
                .collect(Collectors.groupingBy(
                        scan -> truncateToDay(scan.getCreatedAt())
                ))
                .entrySet().stream()
                .map(entry -> {
                    var dto = new DailyUsageReportDto();
                    dto.setDate(Date.from(entry.getKey()));
                    dto.setUsedCards(entry.getValue().stream()
                            .filter(s -> s.getCard() != null)
                            .map(s -> s.getCard().getId())
                            .distinct()
                            .count());
                    return dto;
                })
                .sorted(Comparator.comparing(DailyUsageReportDto::getDate).reversed())
                .toList();
    }

    public List<LocationReportDto> getLocationReport(LocationReportRequest request) {
        var spec = ScanSpec.forLocationReport(
                passedFlags,
                request.getLocationId(),
                request.getStartDate(),
                request.getEndDate()
        );

        return scanRepository.findAll(spec)
                .stream()
                .collect(Collectors.groupingBy(
                        scan -> scan.getReader().getLocation()
                ))
                .entrySet().stream()
                .map(entry -> {
                    var location = entry.getKey();
                    var scans    = entry.getValue();

                    var scansWithCard = scans.stream()
                            .filter(s -> s.getCard() != null)
                            .toList();

                    var dto = new LocationReportDto();
                    dto.setLocationName(location.getName());
                    dto.setTotalScanCount((long) scans.size());
                    dto.setUsedCardCount(scansWithCard.stream()
                            .map(s -> s.getCard().getId())
                            .distinct()
                            .count());
                    return dto;
                })
                .toList();
    }

    public List<MultipleScanReportDto> getMultipleScanReport(MultipleScanReportRequest request) {
        var spec = ScanSpec.forMultipleScansReport(
                duplicateFlags,
                request.getLocationId(),
                request.getStartDate(),
                request.getEndDate()
        );

        return scanRepository.findAll(spec)
                .stream()
                .filter(s -> s.getCard() != null)
                .collect(Collectors.groupingBy(scan -> scan.getCard().getId()))
                .values().stream()
                .filter(scans -> scans.size() > 1)
                .flatMap(scans -> {
                    Instant firstUsedAt = scans.stream()
                            .map(Scan::getCreatedAt)
                            .min(Comparator.naturalOrder())
                            .orElse(null);

                    return scans.stream()
                            .filter(scan -> !scan.getCreatedAt().equals(firstUsedAt))
                            .map(scan -> {
                                var dto = new MultipleScanReportDto();
                                dto.setCardValue(scan.getCard().getValue());
                                dto.setFirstUsedAt(firstUsedAt);
                                dto.setAttemptedAt(scan.getCreatedAt());
                                dto.setLocationName(scan.getReader().getLocation().getName());
                                dto.setReaderUsername(scan.getReader().getUsername());
                                return dto;
                            });
                })
                .sorted(Comparator.comparing(MultipleScanReportDto::getAttemptedAt).reversed())
                .toList();
    }

    public List<ReaderReportDto> getReaderReport(ReaderReportRequest request) {
        var spec = ScanSpec.forReaderReport(
                request.getReaderId(),
                passedFlags,
                request.getStartDate(),
                request.getEndDate()
        );

        return scanRepository.findAll(spec)
                .stream()
                .collect(Collectors.groupingBy(scan -> scan.getReader().getId()))
                .values().stream()
                .map(scans -> {
                    var reader = scans.getFirst().getReader();

                    var dto = new ReaderReportDto();
                    dto.setScannerUsername(reader.getUsername());
                    dto.setLocationName(reader.getLocation().getName());
                    dto.setUsedCards(scans.stream()
                            .filter(s -> s.getCard() != null)
                            .map(s -> s.getCard().getId())
                            .distinct()
                            .count());
                    return dto;
                })
                .sorted(Comparator.comparing(ReaderReportDto::getScannerUsername))
                .toList();
    }

    public Page<SerialNumberReportDto> getSerialNumberReport(SerialNumberReportRequest request, Pageable pageable) {
        var spec = CardSpec.forCardReport(
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus(),
                request.getCardValue()
        );

        var cardPage = cardRepository.findAll(spec, pageable);
        var cards = cardPage.getContent();

        Set<UUID> cardIds = cards.stream().map(Card::getId).collect(Collectors.toSet());
        Set<UUID> usedCardIds = cardIds.isEmpty() ? Set.of() : scanRepository.findUsedCardIds(cardIds, passedFlags);
        Map<UUID, Instant> usedAtMap = cardIds.isEmpty() ? Map.of() : scanRepository.findUsedAtByCardIds(cardIds, passedFlags)
                .stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Instant) row[1]));

        return cardPage.map(card -> {
            var dto = new SerialNumberReportDto();
            dto.setCardValue(card.getValue());
            dto.setCardLabel(card.getLabel());
            dto.setCardType(card.getType().name().toLowerCase());
            dto.setStatus(usedCardIds.contains(card.getId()) ? Status.USED : Status.NOT_USED);
            dto.setUsedAt(usedAtMap.get(card.getId()));
            return dto;
        });
    }

    private Instant truncateToDay(Instant instant) {
        return instant.truncatedTo(ChronoUnit.DAYS);
    }
}
