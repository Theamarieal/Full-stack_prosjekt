package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.TemperatureLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TemperatureLogRepository extends JpaRepository<TemperatureLog, Long> {
    // fetches all logs for an organization by looking through the equipment
    List<TemperatureLog> findAllByEquipmentOrganizationId(Long organizationId);
}