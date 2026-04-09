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

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final ChecklistItemRepository itemRepository;

    /**
     * Returns a paginated list of checklists belonging to the user's organization,
     * optionally filtered by module and/or frequency.
     *
     * @param user      the authenticated user
     * @param page      the page number (0-indexed)
     * @param size      the number of items per page
     * @param module    optional module filter (null = all modules)
     * @param frequency optional frequency filter (null = all frequencies)
     * @return a page of checklists for the user's organization
     */
    public Page<Checklist> getChecklistsByOrganization(User user, int page, int size, ModuleType module, Frequency frequency) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        return checklistRepository.findFiltered(user.getOrganization().getId(), module, frequency, pageable);
    }

    /**
     * Returns all checklists belonging to the user's organization (used by dashboard and manage page).
     *
     * @param user the authenticated user
     * @return list of all checklists for the user's organization
     */
    public List<Checklist> getAllChecklists(User user) {
        return checklistRepository.findAllByOrganizationId(user.getOrganization().getId());
    }

    /**
     * Creates a new checklist for the user's organization.
     *
     * @param request the validated request body
     * @param user    the authenticated manager or admin creating the checklist
     * @return the saved checklist
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
     * @param itemId the id of the item to complete
     * @param user   the authenticated user completing the item
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

    public void deleteChecklist(Long id, User user) {
        Checklist checklist = checklistRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Checklist not found"));

        if (!checklist.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new IllegalArgumentException("Access denied: You can only delete checklists from your own organization");
        }

        checklistRepository.delete(checklist);
    }
}