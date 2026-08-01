package uk.co.eightmile.racs.users.dtos;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String campaignId;
    private String firstName;
    private String lastName;
    private String email;
    private boolean inactive;
    private Instant createdAt;
    private Instant updatedAt;
}
