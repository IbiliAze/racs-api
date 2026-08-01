package uk.co.eightmile.racs.roles;

import uk.co.eightmile.racs.roles.dtos.CreateRoleRequest;
import uk.co.eightmile.racs.roles.dtos.RoleDto;
import uk.co.eightmile.racs.roles.dtos.UpdateRoleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDto toDto(Role role);
    Role toEntity(CreateRoleRequest request);
    void update(UpdateRoleRequest request, @MappingTarget Role role);
}
