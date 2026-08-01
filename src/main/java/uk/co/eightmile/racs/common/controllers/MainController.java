package uk.co.eightmile.racs.common.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "Main")
public class MainController {

    @GetMapping("/health")
    @Operation(summary = "Health endpoint")
    public ResponseEntity<Void> getHealth() {
        return ResponseEntity.ok().build();
    }
}
