package ntnu.no.fs_v26.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.dto.ReportResponse;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for compliance report generation.
 *
 * <p>Provides an endpoint for generating period-based compliance reports
 * aggregating data across all modules (IK-Mat and IK-Alkohol).
 * Access is restricted to users with the {@code MANAGER} or {@code ADMIN} role.
 *
 * <p>Base path: {@code /api/v1/reports}
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Manager/Admin reporting endpoints")
public class ReportController {

    private final ReportService reportService;

    /**
     * Generates a compliance report for the specified date range.
     *
     * <p>The report summarises deviations, temperature logs, checklist completions,
     * and alcohol compliance registrations within the authenticated user's organization.
     *
     * @param user the authenticated manager or admin
     * @param from the start date of the report period (inclusive), in ISO format (YYYY-MM-DD)
     * @param to   the end date of the report period (inclusive), in ISO format (YYYY-MM-DD)
     * @return a {@link ReportResponse} containing the aggregated compliance data
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Generate a compliance report for a date range")
    public ResponseEntity<ReportResponse> getReport(
        @AuthenticationPrincipal User user,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.generateReport(user, from, to));
    }
}