package uk.co.eightmile.racs.campaigns;

import uk.co.eightmile.racs.campaigns.dtos.CreateCampaignRequest;
import uk.co.eightmile.racs.campaigns.dtos.CampaignDto;
import uk.co.eightmile.racs.campaigns.dtos.UpdateCampaignRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CampaignMapper {
    CampaignDto toDto(Campaign campaign);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Campaign toEntity(CreateCampaignRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(UpdateCampaignRequest request, @MappingTarget Campaign campaign);

}
