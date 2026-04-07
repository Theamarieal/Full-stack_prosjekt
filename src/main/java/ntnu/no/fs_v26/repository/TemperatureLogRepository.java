package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.TemperatureLog;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TemperatureLogRepository extends JpaRepository<TemperatureLog, Long> {

    List<TemperatureLog> findAllByEquipmentOrganizationId(Long organizationId);

    List<TemperatureLog> findAllByEquipmentOrganizationIdAndTimestampBetween(
            Long organizationId,
            LocalDateTime from,
            LocalDateTime to);

    List<TemperatureLog> findTop10ByEquipmentOrganizationIdOrderByTimestampDesc(Long organizationId);

    List<TemperatureLog> findTop5ByEquipmentOrganizationIdAndIsDeviationTrueOrderByTimestampDesc(Long organizationId);

    long countByEquipmentOrganizationIdAndTimestampBetween(
            Long organizationId,
            LocalDateTime start,
            LocalDateTime end);

    long countByEquipmentOrganizationIdAndIsDeviationTrueAndTimestampBetween(
            Long organizationId,
            LocalDateTime start,
            LocalDateTime end);

    boolean existsByLoggedBy(User user);
}