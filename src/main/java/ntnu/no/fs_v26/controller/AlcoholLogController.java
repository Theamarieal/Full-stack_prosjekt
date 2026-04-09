package ntnu.no.fs_v26.controller;

import jakarta.validation.Valid;
import ntnu.no.fs_v26.service.AlcoholLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for alcohol compliance log operations.
 *
 * <p>Provides endpoints for creating and retrieving alcohol log entries
 * as part of the IK-Alkohol compliance module. All endpoints require authentication.
 *
 * <p>Base path: {@code /api/v1/alcohol-logs}
 */
@RestController
@RequestMapping("/api/v1/alcohol-logs")
public class AlcoholLogController {

    private final AlcoholLogService alcoholLogService;

    /**
     * Creates a new alcohol log controller instance.
     *
     * @param alcoholLogService the service handling alcohol log business logic
     */
    public AlcoholLogController(AlcoholLogService alcoholLogService) {
        this.alcoholLogService = alcoholLogService;
    }

    /**
     * Creates a new alcohol log entry for the current shift.
     *
     * @param request        the alcohol log request body
     * @param authentication the current authentication object used to identify the user
     * @return the saved {@link AlcoholLogResponse}
     */
    @PostMapping
    public ResponseEntity<AlcoholLogResponse> createLog(
        @Valid @RequestBody AlcoholLogRequest request,
        Authentication authentication) {
        return ResponseEntity.ok(alcoholLogService.createLog(request, authentication.getName()));
    }

    /**
     * Returns alcohol log entries recorded for the current day.
     *
     * @param authentication the current authentication object
     * @return a list of {@link AlcoholLogResponse} entries for today
     */
    @GetMapping("/current-shift")
    public ResponseEntity<List<AlcoholLogResponse>> getCurrentDayLogs(Authentication authentication) {
        return ResponseEntity.ok(alcoholLogService.getCurrentDayLogs(authentication.getName()));
    }

    /**
     * Returns alcohol log entries for a specific date.
     * Intended for use by managers and administrators reviewing historical data.
     *
     * @param date           the date to filter by, in ISO format (YYYY-MM-DD)
     * @param authentication the current authentication object
     * @return a list of {@link AlcoholLogResponse} entries for the given date
     */
    @GetMapping
    public ResponseEntity<List<AlcoholLogResponse>> getLogsByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Authentication authentication) {
        return ResponseEntity.ok(alcoholLogService.getLogsByDate(authentication.getName(), date));
    }

    /**
     * Returns whether any alcohol logs have been recorded for the current day.
     * Used by the dashboard to indicate compliance status.
     *
     * @param authentication the current authentication object
     * @return a map with a {@code hasLogs} boolean field
     */
    @GetMapping("/status")
    public ResponseEntity<?> getTodayStatus(Authentication authentication) {
        boolean hasLogs = alcoholLogService.hasLogsForToday(authentication.getName());
        return ResponseEntity.ok(java.util.Map.of("hasLogs", hasLogs));
    }
}
