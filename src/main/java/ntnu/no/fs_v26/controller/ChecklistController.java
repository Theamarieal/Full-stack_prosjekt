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

@RestController
@RequestMapping("/api/v1/checklists")
@RequiredArgsConstructor
@Tag(name = "Checklists", description = "Endpoints for managing and completing checklists")
public class ChecklistController {

    private final ChecklistService checklistService;

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

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Create a new checklist (Manager/Admin only)")
    public ResponseEntity<Checklist> create(
        @Valid @RequestBody ChecklistRequest request,
        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(checklistService.createChecklist(request, user));
    }

    @PatchMapping("/{id}/items/{itemId}/complete")
    @Operation(summary = "Mark a checklist item as completed")
    public ResponseEntity<Void> completeItem(
        @PathVariable Long itemId,
        @AuthenticationPrincipal User user) {
        checklistService.completeItem(itemId, user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Delete a checklist (Manager/Admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        checklistService.deleteChecklist(id, user);
        return ResponseEntity.noContent().build();
    }
}
