package ntnu.no.fs_v26.config;

import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.model.*;
import ntnu.no.fs_v26.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final OrganizationRepository orgRepository;
        private final ChecklistRepository checklistRepository;
        private final ChecklistItemRepository itemRepository;
        private final EquipmentRepository equipmentRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) {
                if (userRepository.count() > 0)
                        return;

                // create organization
                Organization org = Organization.builder()
                                .name("Testrestaurant AS")
                                .build();
                org = orgRepository.save(org);

                // create users
                String pw = passwordEncoder.encode("password123");
                userRepository.save(User.builder().email("admin@test.no").password(pw).role(Role.ADMIN)
                                .organization(org).build());
                userRepository.save(User.builder().email("manager@test.no").password(pw).role(Role.MANAGER)
                                .organization(org).build());
                userRepository.save(User.builder().email("employee@test.no").password(pw).role(Role.EMPLOYEE)
                                .organization(org).build());

                // create equipment
                equipmentRepository.save(Equipment.builder()
                                .name("Kitchen Fridge")
                                .minTemp(0)
                                .maxTemp(4)
                                .organization(org)
                                .build());

                equipmentRepository.save(Equipment.builder()
                                .name("Cold Storage Fridge")
                                .minTemp(0)
                                .maxTemp(4)
                                .organization(org)
                                .build());

                equipmentRepository.save(Equipment.builder()
                                .name("Freezer Room")
                                .minTemp(-25)
                                .maxTemp(-18)
                                .organization(org)
                                .build());

                equipmentRepository.save(Equipment.builder()
                                .name("Hot Holding Unit")
                                .minTemp(60)
                                .maxTemp(90)
                                .organization(org)
                                .build());

                // create checklist
                Checklist checklist = Checklist.builder()
                                .title("Morningroutine Kitchen")
                                .description("Daily tasks before opening")
                                .frequency(Frequency.DAILY)
                                .module(ModuleType.KITCHEN)
                                .organization(org)
                                .build();
                checklist = checklistRepository.save(checklist);

                // create checklist items
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

                System.out.println("DEBUG: All test data (users, equipment, and checklists) has been added!");
        }
}