package ntnu.no.fs_v26.config;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.*;
import ntnu.no.fs_v26.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ntnu.no.fs_v26.model.Frequency;
import ntnu.no.fs_v26.model.ModuleType;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrganizationRepository orgRepository;
    private final ChecklistRepository checklistRepository;
    private final ChecklistItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        // create organization
        Organization org = Organization.builder()
                .name("Testrestaurant AS")
                .build();
        org = orgRepository.save(org);

        // create users
        String pw = passwordEncoder.encode("password123");
        userRepository.save(User.builder().email("admin@test.no").password(pw).role(Role.ADMIN).organization(org).build());
        userRepository.save(User.builder().email("manager@test.no").password(pw).role(Role.MANAGER).organization(org).build());
        userRepository.save(User.builder().email("employee@test.no").password(pw).role(Role.EMPLOYEE).organization(org).build());

        // create checklist (use enum-values instead of String)
        Checklist checklist = Checklist.builder()
                .title("Morningroutine Kitchen")
                .description("Daily tasks before opening")
                .frequency(Frequency.DAILY)
                .module(ModuleType.KITCHEN)
                .organization(org)
                .build();
        checklist = checklistRepository.save(checklist);

        // create checklist-items
        itemRepository.save(ChecklistItem.builder()
                .description("Check temperature in fridge")
                .completed(false)
                .checklist(checklist)
                .build());

        itemRepository.save(ChecklistItem.builder()
                .description("Turn on coffeemachine")
                .completed(false)
                .checklist(checklist)
                .build());

        System.out.println("DEBUG: All testdata (users and checklists) are now added!");
    }
}