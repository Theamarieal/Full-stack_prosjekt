package ntnu.no.fs_v26.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.DeviationStatus;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.service.DeviationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for deviation reporting and management.
 *
 * <p>Allows all authenticated users to report and view deviations.
 * Status updates are restricted to users with the {@code MANAGER} or {@code ADMIN} role.
 * All data is scoped to the authenticated user's organization.
 *
 * <p>Base path: {@code /api/v1/deviations}
 */
@RestController
@RequestMapping("/api/v1/deviations")
@RequiredArgsConstructor
@Tag(name = "Deviations", description = "Endpoints for reporting and managing non-compliance issues")
public class DeviationController {

    private final DeviationService service;

    /**
     * Returns a paginated list of deviations for the authenticated user's organization.
     *
     * @param user the authenticated user
     * @param page the page number to retrieve (zero-indexed, default 0)
     * @param size the number of items per page (default 5)
     * @return a paginated list of {@link Deviation} objects
     */
    @GetMapping
    @Operation(summary = "Get paginated deviations for your organization")
    public ResponseEntity<Page<Deviation>> getAll(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(service.getDeviations(user, page, size));
    }

    /**
     * Reports a new deviation for the authenticated user's organization.
     *
     * @param request the deviation data including title, description, and module
     * @param user    the authenticated user reporting the deviation
     * @return the created {@link Deviation}
     */
    @PostMapping
    @Operation(summary = "Report a new deviation")
    public ResponseEntity<Deviation> create(
        @Valid @RequestBody DeviationRequest request,
        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.reportDeviation(request, user));
    }

    /**
     * Updates the status of an existing deviation.
     * Requires the {@code MANAGER} or {@code ADMIN} role.
     *
     * @param id     the ID of the deviation to update
     * @param status the new status (e.g. OPEN, IN_PROGRESS, CLOSED)
     * @param user   the authenticated manager or admin
     * @return the updated {@link Deviation}
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Update deviation status (Manager/Admin only)")
    public ResponseEntity<Deviation> updateStatus(
        @PathVariable Long id,
        @RequestParam DeviationStatus status,
        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.updateStatus(id, status, user));
    }
}