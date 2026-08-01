package uk.co.eightmile.racs.roles.dtos;

import uk.co.eightmile.racs.common.dtos.GetAllResponse;
import uk.co.eightmile.racs.roles.dtos.RoleDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetRolesResponse extends GetAllResponse {
    private List<RoleDto> roles;
}
