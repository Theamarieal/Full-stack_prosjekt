package ntnu.no.fs_v26.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.*;
import ntnu.no.fs_v26.service.TemperatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/temperature-logs")
@RequiredArgsConstructor
@Tag(name = "Temperature Logs", description = "Endpoints for food safety temperature monitoring")
public class TemperatureController {

    private final TemperatureService service;

    @PostMapping
    @Operation(summary = "Log a new temperature reading")
    public ResponseEntity<TemperatureLog> create(
            @RequestParam Long equipmentId,
            @RequestParam double value,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.logTemperature(equipmentId, value, user));
    }

    @GetMapping
    @Operation(summary = "Get all logs for your organization")
    public ResponseEntity<List<TemperatureLog>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getLogsForOrganization(user));
    }
}