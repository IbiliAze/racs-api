package uk.co.eightmile.racs.readers.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReaderLoginRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
