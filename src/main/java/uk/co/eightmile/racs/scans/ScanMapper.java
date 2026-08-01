package uk.co.eightmile.racs.scans;

import uk.co.eightmile.racs.cards.CardMapper;
import uk.co.eightmile.racs.scans.dtos.CreateScanRequest;
import uk.co.eightmile.racs.scans.dtos.ScanDto;
import uk.co.eightmile.racs.scans.dtos.ScanWithCardDto;
import uk.co.eightmile.racs.scans.dtos.UpdateScanRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {CardMapper.class})
public interface ScanMapper {

    @Mapping(source = "reader.id", target = "readerId")
    @Mapping(source = "card.id", target = "cardId")
    ScanDto toDto(Scan scan);

    @Mapping(source = "reader.id", target = "readerId")
    ScanWithCardDto toScanWithCardDto(Scan scan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reader", ignore = true)
    @Mapping(target = "card", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Scan toEntity(CreateScanRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reader", ignore = true)
    @Mapping(target = "card", ignore = true)
    @Mapping(target = "scannedValue", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(UpdateScanRequest request, @MappingTarget Scan scan);
}
