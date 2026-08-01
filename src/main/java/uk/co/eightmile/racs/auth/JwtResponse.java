package uk.co.eightmile.racs.auth;

import uk.co.eightmile.racs.common.dtos.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse extends Response {
    private String token;
}
