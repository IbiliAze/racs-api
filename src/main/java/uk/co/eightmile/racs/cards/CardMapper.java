package uk.co.eightmile.racs.cards;

import uk.co.eightmile.racs.cards.dtos.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "campaignId", source = "campaign.id")
    @Mapping(target = "type", source = "type", qualifiedByName = "typeToString")
    @Mapping(target = "used", ignore = true)
    @Mapping(target = "usedAt", ignore = true)
    @Mapping(target = "validFrom", ignore = true)
    @Mapping(target = "validUntil", ignore = true)
    CardDto toDto(Card card);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "invalidated", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToType")
    Card toEntity(CreateCardRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    @Mapping(target = "type", source = "type", qualifiedByName = "stringToType")
    void update(UpdateCardRequest request, @MappingTarget Card card);

    @Named("typeToString")
    default String typeToString(CardType type) {
        return type == null ? null : type.name().toLowerCase();
    }

    @Named("stringToType")
    default CardType stringToType(String type) {
        return type == null ? null : CardType.valueOf(type.toUpperCase());
    }
}
