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

                String pw = passwordEncoder.encode("password123");

                // ── Organization 1: Testrestaurant AS ────────────────────────────────────
                Organization org1 = orgRepository.save(Organization.builder()
                    .name("Testrestaurant AS")
                    .build());

                userRepository.save(User.builder().email("admin@test.no").password(pw)
                    .role(Role.ADMIN).organization(org1).build());
                userRepository.save(User.builder().email("manager@test.no").password(pw)
                    .role(Role.MANAGER).organization(org1).build());
                userRepository.save(User.builder().email("employee@test.no").password(pw)
                    .role(Role.EMPLOYEE).organization(org1).build());

                equipmentRepository.save(Equipment.builder()
                    .name("Kitchen Fridge").minTemp(0).maxTemp(4).organization(org1).build());
                equipmentRepository.save(Equipment.builder()
                    .name("Cold Storage Fridge").minTemp(0).maxTemp(4).organization(org1).build());
                equipmentRepository.save(Equipment.builder()
                    .name("Freezer Room").minTemp(-25).maxTemp(-18).organization(org1).build());
                equipmentRepository.save(Equipment.builder()
                    .name("Hot Holding Unit").minTemp(60).maxTemp(90).organization(org1).build());

                Checklist checklist1 = checklistRepository.save(Checklist.builder()
                    .title("Morningroutine Kitchen")
                    .description("Daily tasks before opening")
                    .frequency(Frequency.DAILY)
                    .module(ModuleType.KITCHEN)
                    .organization(org1)
                    .build());
                itemRepository.save(ChecklistItem.builder()
                    .description("Check temperature in fridge").completed(false).checklist(checklist1).build());
                itemRepository.save(ChecklistItem.builder()
                    .description("Turn on coffeemachine").completed(false).checklist(checklist1).build());

                // ── Organization 2: Testrestaurant 2 AS ──────────────────────────────────
                Organization org2 = orgRepository.save(Organization.builder()
                    .name("Testrestaurant 2 AS")
                    .build());

                userRepository.save(User.builder().email("admin@test2.no").password(pw)
                    .role(Role.ADMIN).organization(org2).build());
                userRepository.save(User.builder().email("manager@test2.no").password(pw)
                    .role(Role.MANAGER).organization(org2).build());
                userRepository.save(User.builder().email("employee@test2.no").password(pw)
                    .role(Role.EMPLOYEE).organization(org2).build());

                equipmentRepository.save(Equipment.builder()
                    .name("Kitchen Fridge").minTemp(0).maxTemp(4).organization(org2).build());
                equipmentRepository.save(Equipment.builder()
                    .name("Freezer Room").minTemp(-25).maxTemp(-18).organization(org2).build());

                Checklist checklist2 = checklistRepository.save(Checklist.builder()
                    .title("Evening Routine Bar")
                    .description("Daily tasks before closing")
                    .frequency(Frequency.DAILY)
                    .module(ModuleType.BAR)
                    .organization(org2)
                    .build());
                itemRepository.save(ChecklistItem.builder()
                    .description("Check temperature in fridge").completed(false).checklist(checklist2).build());
                itemRepository.save(ChecklistItem.builder()
                    .description("Turn on coffeemachine").completed(false).checklist(checklist2).build());

                System.out.println("DEBUG: All test data (users, equipment, and checklists) has been added!");
        }
}