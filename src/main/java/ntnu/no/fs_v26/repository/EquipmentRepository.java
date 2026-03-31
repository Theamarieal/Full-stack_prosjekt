package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    // fetches all equipment that belongs to a spesific organization
    List<Equipment> findAllByOrganizationId(Long organizationId);
}