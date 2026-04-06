package ntnu.no.fs_v26.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.Checklist;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.service.ChecklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/checklists")
@RequiredArgsConstructor
@Tag(name = "Checklists", description = "Endpoints for managing and completing checklists")
public class ChecklistController {

    private final ChecklistService checklistService;

    @GetMapping
    @Operation(summary = "Get all checklists for your organization")
    public ResponseEntity<List<Checklist>> getAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(checklistService.getChecklistsByOrganization(user));
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
}
