package ntnu.no.fs_v26.service;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.Checklist;
import ntnu.no.fs_v26.model.ChecklistItem;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.ChecklistRepository;
import ntnu.no.fs_v26.repository.ChecklistItemRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistService {
    private final ChecklistRepository checklistRepository;
    private final ChecklistItemRepository itemRepository;

    public List<Checklist> getChecklistsByOrganization(User user) {
        // fetches only checklists that belong to the users organization
        return checklistRepository.findAllByOrganizationId(user.getOrganization().getId());
    }

    public Checklist createChecklist(Checklist checklist, User user) {
        checklist.setOrganization(user.getOrganization());
        return checklistRepository.save(checklist);
    }

    public void completeItem(Long itemId, User user) {
        ChecklistItem item = itemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item not found"));
        
        // security: check that item belongs to users organization
        if (!item.getChecklist().getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access to checklist item");
        }

        item.setCompleted(true);
        item.setCompletedAt(LocalDateTime.now());
        item.setCompletedBy(user);
        itemRepository.save(item);
    }
}