package uk.co.eightmile.racs.readers;

import uk.co.eightmile.racs.auth.dtos.JwtPrincipalDto;
import uk.co.eightmile.racs.readers.dtos.CreateReaderRequest;
import uk.co.eightmile.racs.readers.dtos.ReaderDto;
import uk.co.eightmile.racs.readers.dtos.UpdateReaderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ReaderMapper {
    @Mapping(target = "locationId", source = "location.id")
    ReaderDto toDto(Reader reader);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "scans", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Reader toEntity(CreateReaderRequest request);

    @Mapping(target = "authorities", expression = "java(java.util.List.of(\"READER\"))")
    JwtPrincipalDto toJwtPrincipal(Reader reader);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "scans", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(UpdateReaderRequest request, @MappingTarget Reader reader);
}