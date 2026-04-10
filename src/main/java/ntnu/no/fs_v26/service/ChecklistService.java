package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.controller.ChecklistRequest;
import ntnu.no.fs_v26.model.Checklist;
import ntnu.no.fs_v26.model.ChecklistItem;
import ntnu.no.fs_v26.model.Frequency;
import ntnu.no.fs_v26.model.ModuleType;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.ChecklistItemRepository;
import ntnu.no.fs_v26.repository.ChecklistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for checklist management and completion.
 *
 * <p>Handles retrieval, creation, item completion, and deletion of checklists.
 * All operations are scoped to the authenticated user's organization,
 * enforcing multi-tenancy across both IK-Mat and IK-Alkohol modules.
 */
@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final ChecklistItemRepository itemRepository;

    /**
     * Returns a paginated list of checklists for the user's organization,
     * optionally filtered by module type and frequency.
     *
     * @param user      the authenticated user
     * @param page      the page number (zero-indexed)
     * @param size      the number of items per page
     * @param module    optional module filter; {@code null} returns all modules
     * @param frequency optional frequency filter; {@code null} returns all frequencies
     * @return a page of checklists for the user's organization
     */
    public Page<Checklist> getChecklistsByOrganization(User user, int page, int size, ModuleType module, Frequency frequency) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        return checklistRepository.findFiltered(user.getOrganization().getId(), module, frequency, pageable);
    }

    /**
     * Returns all checklists for the user's organization.
     * Used by the dashboard and the checklist management view.
     *
     * @param user the authenticated user
     * @return a list of all checklists for the organization
     */
    public List<Checklist> getAllChecklists(User user) {
        return checklistRepository.findAllByOrganizationId(user.getOrganization().getId());
    }

    /**
     * Creates a new checklist and its items for the user's organization.
     *
     * @param request the validated request body containing title, description, frequency, module, and items
     * @param user    the authenticated manager or admin creating the checklist
     * @return the saved checklist (without items populated in response)
     */
    public Checklist createChecklist(ChecklistRequest request, User user) {
        Checklist checklist = Checklist.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .frequency(request.getFrequency())
            .module(request.getModule())
            .organization(user.getOrganization())
            .build();

        Checklist savedChecklist = checklistRepository.save(checklist);

        if (request.getItems() != null) {
            request.getItems().forEach(itemTitle -> {
                ChecklistItem item = ChecklistItem.builder()
                    .description(itemTitle)
                    .checklist(savedChecklist)
                    .completed(false)
                    .build();
                itemRepository.save(item);
            });
        }

        return savedChecklist;
    }

    /**
     * Marks a checklist item as completed by the given user.
     *
     * <p>Verifies that the item belongs to the user's organization before completing it.
     *
     * @param itemId the ID of the checklist item to complete
     * @param user   the authenticated user completing the item
     * @throws IllegalArgumentException if the item is not found or belongs to a different organization
     */
    public void completeItem(Long itemId, User user) {
        ChecklistItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Checklist item not found"));

        if (!item.getChecklist().getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new IllegalArgumentException("Access denied: item belongs to a different organization");
        }

        item.setCompleted(true);
        item.setCompletedAt(LocalDateTime.now());
        item.setCompletedBy(user);
        itemRepository.save(item);
    }

    /**
     * Deletes a checklist by ID.
     *
     * <p>Verifies that the checklist belongs to the user's organization before deleting.
     *
     * @param id   the ID of the checklist to delete
     * @param user the authenticated manager or admin
     * @throws IllegalArgumentException if the checklist is not found or belongs to a different organization
     */
    public void deleteChecklist(Long id, User user) {
        Checklist checklist = checklistRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Checklist not found"));

        if (!checklist.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new IllegalArgumentException("Access denied: You can only delete checklists from your own organization");
        }

        checklistRepository.delete(checklist);
    }
}