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

/**
 * REST controller for temperature logging and monitoring.
 *
 * <p>Provides endpoints for recording temperature readings and retrieving
 * log history as part of the IK-Mat food safety compliance module.
 * All data is scoped to the authenticated user's organization.
 *
 * <p>Base path: {@code /api/v1/temperature-logs}
 */
@RestController
@RequestMapping("/api/v1/temperature-logs")
@RequiredArgsConstructor
@Tag(name = "Temperature Logs", description = "Endpoints for food safety temperature monitoring")
public class TemperatureController {

    private final TemperatureService service;

    /**
     * Records a new temperature reading for a specific piece of equipment.
     *
     * @param equipmentId the ID of the equipment being logged
     * @param value       the recorded temperature value in degrees Celsius
     * @param user        the authenticated user recording the reading
     * @return the saved {@link TemperatureLog}
     */
    @PostMapping
    @Operation(summary = "Log a new temperature reading")
    public ResponseEntity<TemperatureLog> create(
        @RequestParam Long equipmentId,
        @RequestParam double value,
        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.logTemperature(equipmentId, value, user));
    }

    /**
     * Returns all temperature logs for the authenticated user's organization.
     *
     * @param user the authenticated user
     * @return a list of all {@link TemperatureLog} entries for the organization
     */
    @GetMapping
    @Operation(summary = "Get all logs for your organization")
    public ResponseEntity<List<TemperatureLog>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getLogsForOrganization(user));
    }

    /**
     * Returns the most recent temperature log entries for the organization.
     *
     * @param user  the authenticated user
     * @param limit the maximum number of entries to return (default 10)
     * @return a list of the latest {@link TemperatureLog} entries
     */
    @GetMapping("/latest")
    @Operation(summary = "Get latest temperature logs")
    public ResponseEntity<List<TemperatureLog>> getLatest(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getLatestLogs(user, limit));
    }

    /**
     * Returns filtered temperature log history for the organization.
     *
     * @param user        the authenticated user
     * @param equipmentId optional filter by equipment ID
     * @param fromDate    optional start date filter (ISO format)
     * @param toDate      optional end date filter (ISO format)
     * @param status      optional filter by deviation status (e.g. "DEVIATION")
     * @return a filtered list of {@link TemperatureLog} entries
     */
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

    /**
     * Returns a dashboard summary of temperature status across all equipment.
     * Used to indicate whether any equipment is currently out of acceptable range.
     *
     * @param user the authenticated user
     * @return a map containing summary data such as deviation counts and equipment status
     */
    @GetMapping("/summary")
    @Operation(summary = "Get dashboard summary for temperature logs")
    public ResponseEntity<Map<String, Object>> getSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getSummary(user));
    }

    /**
     * Returns the most recent temperature deviation entries for the organization.
     *
     * @param user  the authenticated user
     * @param limit the maximum number of deviation entries to return (default 5)
     * @return a list of the latest {@link TemperatureLog} entries flagged as deviations
     */
    @GetMapping("/deviations/latest")
    @Operation(summary = "Get latest temperature deviations")
    public ResponseEntity<List<TemperatureLog>> getLatestDeviations(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(service.getLatestDeviations(user, limit));
    }
}