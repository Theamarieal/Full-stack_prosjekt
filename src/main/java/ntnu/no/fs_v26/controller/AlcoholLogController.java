package ntnu.no.fs_v26.controller;

import ntnu.no.fs_v26.service.AlcoholLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for alcohol log operations.
 */
@RestController
@RequestMapping("/api/v1/alcohol-logs")
public class AlcoholLogController {

    /**
     * Service used for alcohol log business logic.
     */
    private final AlcoholLogService alcoholLogService;

    /**
     * Creates a new alcohol log controller instance.
     *
     * @param alcoholLogService the alcohol log service
     */
    public AlcoholLogController(AlcoholLogService alcoholLogService) {
        this.alcoholLogService = alcoholLogService;
    }

    /**
     * Creates a new alcohol log entry for the current day.
     *
     * @param request        the alcohol log request body
     * @param authentication the current authentication object
     * @return the saved alcohol log
     */
    @PostMapping
    public ResponseEntity<AlcoholLogResponse> createLog(
            @RequestBody AlcoholLogRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(alcoholLogService.createLog(request, authentication.getName()));
    }

    /**
     * Returns alcohol logs for the current day.
     *
     * @param authentication the current authentication object
     * @return a list of alcohol logs for the current day
     */
    @GetMapping("/current-shift")
    public ResponseEntity<List<AlcoholLogResponse>> getCurrentDayLogs(Authentication authentication) {
        return ResponseEntity.ok(alcoholLogService.getCurrentDayLogs(authentication.getName()));
    }

    /**
     * Returns alcohol logs for a specific date.
     *
     * <p>
     * This endpoint is intended for managers and administrators.
     *
     * @param date           the date to search for
     * @param authentication the current authentication object
     * @return a list of alcohol logs for the selected date
     */
    @GetMapping
    public ResponseEntity<List<AlcoholLogResponse>> getLogsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        return ResponseEntity.ok(alcoholLogService.getLogsByDate(authentication.getName(), date));
    }

    /**
     * Returns whether alcohol logs exist for the current day.
     *
     * @param authentication the current authentication object
     * @return an object describing whether logs exist
     */
    @GetMapping("/status")
    public ResponseEntity<?> getTodayStatus(Authentication authentication) {
        boolean hasLogs = alcoholLogService.hasLogsForToday(authentication.getName());

        return ResponseEntity.ok(java.util.Map.of(
                "hasLogs", hasLogs));
    }
}