package uk.co.eightmile.racs.users;

import uk.co.eightmile.racs.auth.dtos.JwtPrincipalDto;
import uk.co.eightmile.racs.users.dtos.CreateUserRequest;
import uk.co.eightmile.racs.users.dtos.UpdateUserRequest;
import uk.co.eightmile.racs.users.dtos.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {
    @Mapping(target = "campaignId", source = "campaign.id")
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "authorities", expression = "java(java.util.List.of(\"ADMIN\"))")
    @Mapping(target = "campaignId", source = "campaign.id")
//    @Mapping(target = "authorities", expression =
//            "java(user.getPermissions().stream().map(permission -> permission.getId().name()).toList())")
    JwtPrincipalDto toJwtPrincipal(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(UpdateUserRequest request, @MappingTarget User user);
}