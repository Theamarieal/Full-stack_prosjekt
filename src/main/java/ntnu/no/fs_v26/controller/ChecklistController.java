package ntnu.no.fs_v26.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.Checklist;
import ntnu.no.fs_v26.model.Frequency;
import ntnu.no.fs_v26.model.ModuleType;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.service.ChecklistService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for checklist management and completion.
 *
 * <p>Provides endpoints for retrieving, creating, completing, and deleting checklists.
 * All endpoints are scoped to the authenticated user's organization.
 * Creating and deleting checklists requires the {@code MANAGER} or {@code ADMIN} role.
 *
 * <p>Base path: {@code /api/v1/checklists}
 */
@RestController
@RequestMapping("/api/v1/checklists")
@RequiredArgsConstructor
@Tag(name = "Checklists", description = "Endpoints for managing and completing checklists")
public class ChecklistController {

    private final ChecklistService checklistService;

    /**
     * Returns a paginated list of checklists for the authenticated user's organization,
     * with optional filtering by module type and frequency.
     *
     * @param user      the authenticated user
     * @param page      the page number to retrieve (zero-indexed, default 0)
     * @param size      the number of items per page (default 6)
     * @param module    optional filter by module type (e.g. KITCHEN, BAR)
     * @param frequency optional filter by frequency (e.g. DAILY, WEEKLY)
     * @return a paginated list of checklists
     */
    @GetMapping
    @Operation(summary = "Get paginated checklists, optionally filtered by module and frequency")
    public ResponseEntity<Page<Checklist>> getAll(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "6") int size,
        @RequestParam(required = false) ModuleType module,
        @RequestParam(required = false) Frequency frequency) {
        return ResponseEntity.ok(checklistService.getChecklistsByOrganization(user, page, size, module, frequency));
    }

    /**
     * Creates a new checklist for the authenticated user's organization.
     * Requires the {@code MANAGER} or {@code ADMIN} role.
     *
     * @param request the checklist data including title, description, frequency, and items
     * @param user    the authenticated manager or admin
     * @return the created {@link Checklist}
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Create a new checklist (Manager/Admin only)")
    public ResponseEntity<Checklist> create(
        @Valid @RequestBody ChecklistRequest request,
        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(checklistService.createChecklist(request, user));
    }

    /**
     * Marks a specific checklist item as completed by the authenticated user.
     *
     * @param itemId the ID of the checklist item to complete
     * @param user   the authenticated user
     * @return HTTP 200 on success
     */
    @PatchMapping("/{id}/items/{itemId}/complete")
    @Operation(summary = "Mark a checklist item as completed")
    public ResponseEntity<Void> completeItem(
        @PathVariable Long itemId,
        @AuthenticationPrincipal User user) {
        checklistService.completeItem(itemId, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes a checklist by ID.
     * Requires the {@code MANAGER} or {@code ADMIN} role.
     *
     * @param id   the ID of the checklist to delete
     * @param user the authenticated manager or admin
     * @return HTTP 204 No Content on success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Delete a checklist (Manager/Admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        checklistService.deleteChecklist(id, user);
        return ResponseEntity.noContent().build();
    }
}