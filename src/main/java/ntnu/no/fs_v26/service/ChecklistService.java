package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.controller.ChecklistRequest;
import ntnu.no.fs_v26.model.Checklist;
import ntnu.no.fs_v26.model.ChecklistItem;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.ChecklistItemRepository;
import ntnu.no.fs_v26.repository.ChecklistRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistService {

    private final ChecklistRepository checklistRepository;
    private final ChecklistItemRepository itemRepository;

    /**
     * Returns all checklists belonging to the user's organization.
     *
     * @param user the authenticated user
     * @return list of checklists for the user's organization
     */
    public List<Checklist> getChecklistsByOrganization(User user) {
        return checklistRepository.findAllByOrganizationId(user.getOrganization().getId());
    }

    /**
     * Creates a new checklist for the user's organization.
     *
     * <p>Accepts a {@link ChecklistRequest} DTO instead of the entity directly
     * to prevent mass-assignment — callers cannot override organization or id.
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

        return checklistRepository.save(checklist);
    }

    /**
     * Marks a checklist item as completed by the given user.
     *
     * <p>Verifies that the item belongs to the user's organization before
     * allowing completion — prevents cross-organization data manipulation.
     *
     * @param itemId the id of the item to complete
     * @param user   the authenticated user completing the item
     * @throws IllegalArgumentException if the item is not found or belongs to a different org
     */
    public void completeItem(Long itemId, User user) {
        ChecklistItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Checklist item not found"));

        // Security: verify item belongs to the user's organization
        if (!item.getChecklist().getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new IllegalArgumentException("Access denied: item belongs to a different organization");
        }

        item.setCompleted(true);
        item.setCompletedAt(LocalDateTime.now());
        item.setCompletedBy(user);
        itemRepository.save(item);
    }
}
