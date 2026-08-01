package uk.co.eightmile.racs.auth.dtos;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtPrincipalDto {
    private UUID id;
    private String campaignId;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> authorities;
}
