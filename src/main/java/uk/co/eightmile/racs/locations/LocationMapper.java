package uk.co.eightmile.racs.locations;

import uk.co.eightmile.racs.locations.dtos.CreateLocationRequest;
import uk.co.eightmile.racs.locations.dtos.LocationDto;
import uk.co.eightmile.racs.locations.dtos.UpdateLocationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    LocationDto toDto(Location location);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campaigns", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Location toEntity(CreateLocationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campaigns", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(UpdateLocationRequest request, @MappingTarget Location location);
}