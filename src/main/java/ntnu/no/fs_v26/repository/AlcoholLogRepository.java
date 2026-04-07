package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.AlcoholLog;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlcoholLogRepository extends JpaRepository<AlcoholLog, Long> {

    List<AlcoholLog> findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
            Long organizationId,
            LocalDate shiftDate
    );

    List<AlcoholLog> findByOrganizationIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            Long organizationId,
            LocalDateTime from,
            LocalDateTime to
    );

    boolean existsByRecordedBy(User user);
}