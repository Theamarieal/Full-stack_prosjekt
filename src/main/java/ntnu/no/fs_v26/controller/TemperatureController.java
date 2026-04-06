package ntnu.no.fs_v26.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.TemperatureLog;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.service.TemperatureService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/latest")
    @Operation(summary = "Get latest temperature logs")
    public ResponseEntity<List<TemperatureLog>> getLatest(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getLatestLogs(user, limit));
    }

    @GetMapping("/history")
    @Operation(summary = "Get filtered temperature history")
    public ResponseEntity<List<TemperatureLog>> getHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(service.getHistory(user, equipmentId, fromDate, toDate, status));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary for temperature logs")
    public ResponseEntity<Map<String, Object>> getSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getSummary(user));
    }

    @GetMapping("/deviations/latest")
    @Operation(summary = "Get latest temperature deviations")
    public ResponseEntity<List<TemperatureLog>> getLatestDeviations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(service.getLatestDeviations(user, limit));
    }
}