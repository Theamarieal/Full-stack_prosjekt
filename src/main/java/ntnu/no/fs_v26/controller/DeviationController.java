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

@RestController
@RequestMapping("/api/v1/deviations")
@RequiredArgsConstructor
@Tag(name = "Deviations", description = "Endpoints for reporting and managing non-compliance issues")
public class DeviationController {

    private final DeviationService service;

    @GetMapping
    @Operation(summary = "Get paginated deviations for your organization")
    public ResponseEntity<Page<Deviation>> getAll(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(service.getDeviations(user, page, size));
    }

    @PostMapping
    @Operation(summary = "Report a new deviation")
    public ResponseEntity<Deviation> create(
        @Valid @RequestBody DeviationRequest request,
        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.reportDeviation(request, user));
    }

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