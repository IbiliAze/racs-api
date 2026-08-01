package uk.co.eightmile.racs.users.dtos;

import uk.co.eightmile.racs.common.dtos.GetAllResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetUsersResponse extends GetAllResponse {
    private List<UserDto> users;
}
