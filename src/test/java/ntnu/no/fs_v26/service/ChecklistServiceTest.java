package ntnu.no.fs_v26.service;

import ntnu.no.fs_v26.controller.ChecklistRequest;
import ntnu.no.fs_v26.model.*;
import ntnu.no.fs_v26.repository.ChecklistItemRepository;
import ntnu.no.fs_v26.repository.ChecklistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

    @Mock
    private ChecklistRepository checklistRepository;

    @Mock
    private ChecklistItemRepository itemRepository;

    @InjectMocks
    private ChecklistService checklistService;

    private Organization org;
    private User manager;
    private User otherOrgEmployee;

    @BeforeEach
    void setUp() {
        org = Organization.builder().build();
        org = setId(org, 1L);

        Organization otherOrg = Organization.builder().build();
        otherOrg = setId(otherOrg, 2L);

        manager = User.builder()
                .email("manager@test.no")
                .role(Role.MANAGER)
                .organization(org)
                .build();

        otherOrgEmployee = User.builder()
                .email("other@test.no")
                .role(Role.EMPLOYEE)
                .organization(otherOrg)
                .build();
    }

    // Helper — setter id via reflection siden Lombok @Builder ikke tar id
    private <T> T setId(T obj, Long id) {
        try {
            var field = obj.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(obj, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return obj;
    }

    // ── createChecklist ───────────────────────────────────────────────────

    @Test
    void createChecklist_shouldSaveAndReturnChecklist() {
        ChecklistRequest request = new ChecklistRequest();
        request.setTitle("Morning routine");
        request.setFrequency(Frequency.DAILY);
        request.setModule(ModuleType.KITCHEN);

        Checklist saved = Checklist.builder()
                .title("Morning routine")
                .organization(org)
                .build();

        when(checklistRepository.save(any(Checklist.class))).thenReturn(saved);

        Checklist result = checklistService.createChecklist(request, manager);

        assertNotNull(result);
        assertEquals("Morning routine", result.getTitle());
        verify(checklistRepository, times(1)).save(any(Checklist.class));
    }

    @Test
    void createChecklist_withItems_shouldSaveItems() {
        ChecklistRequest request = new ChecklistRequest();
        request.setTitle("With items");
        request.setItems(List.of("Clean fridge", "Check temperature"));

        Checklist saved = Checklist.builder().title("With items").organization(org).build();
        when(checklistRepository.save(any())).thenReturn(saved);
        when(itemRepository.save(any())).thenReturn(new ChecklistItem());

        checklistService.createChecklist(request, manager);

        // Two items should be saved
        verify(itemRepository, times(2)).save(any(ChecklistItem.class));
    }

    @Test
    void createChecklist_organizationIsAlwaysTakenFromUser() {
        ChecklistRequest request = new ChecklistRequest();
        request.setTitle("Test");

        when(checklistRepository.save(any(Checklist.class))).thenAnswer(inv -> inv.getArgument(0));

        Checklist result = checklistService.createChecklist(request, manager);

        // Organization must come from the user, never from the request
        assertEquals(org, result.getOrganization());
    }

    // ── completeItem ──────────────────────────────────────────────────────

    @Test
    void completeItem_shouldMarkAsCompletedAndSetTimestamp() {
        Organization itemOrg = Organization.builder().build();
        setId(itemOrg, 1L); // same org as manager

        Checklist checklist = Checklist.builder().organization(itemOrg).build();
        ChecklistItem item = ChecklistItem.builder()
                .description("Mop floor")
                .checklist(checklist)
                .completed(false)
                .build();

        when(itemRepository.findById(42L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);

        checklistService.completeItem(42L, manager);

        assertTrue(item.isCompleted());
        assertNotNull(item.getCompletedAt());
        assertEquals(manager, item.getCompletedBy());
        verify(itemRepository).save(item);
    }

    @Test
    void completeItem_itemNotFound_shouldThrowIllegalArgument() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> checklistService.completeItem(99L, manager));
    }

    @Test
    void completeItem_itemFromOtherOrg_shouldThrowIllegalArgument() {
        Organization otherOrg = Organization.builder().build();
        setId(otherOrg, 2L);

        Checklist otherChecklist = Checklist.builder().organization(otherOrg).build();
        ChecklistItem item = ChecklistItem.builder()
                .description("Secret task")
                .checklist(otherChecklist)
                .completed(false)
                .build();

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> checklistService.completeItem(10L, manager));
    }

    // ── deleteChecklist ───────────────────────────────────────────────────

    @Test
    void deleteChecklist_shouldDeleteWhenOwnedByOrg() {
        Checklist checklist = Checklist.builder()
                .title("To delete")
                .organization(org)
                .build();

        when(checklistRepository.findById(5L)).thenReturn(Optional.of(checklist));

        checklistService.deleteChecklist(5L, manager);

        verify(checklistRepository).delete(checklist);
    }

    @Test
    void deleteChecklist_notFound_shouldThrowIllegalArgument() {
        when(checklistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> checklistService.deleteChecklist(99L, manager));
    }

    @Test
    void deleteChecklist_fromOtherOrg_shouldThrowIllegalArgument() {
        Organization otherOrg = Organization.builder().build();
        setId(otherOrg, 2L);

        Checklist otherChecklist = Checklist.builder()
                .title("Not mine")
                .organization(otherOrg)
                .build();

        when(checklistRepository.findById(7L)).thenReturn(Optional.of(otherChecklist));

        assertThrows(IllegalArgumentException.class,
                () -> checklistService.deleteChecklist(7L, manager));
        verify(checklistRepository, never()).delete(any());
    }
}
