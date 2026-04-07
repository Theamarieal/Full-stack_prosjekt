package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviationRepository extends JpaRepository<Deviation, Long> {

    List<Deviation> findAllByOrganizationId(Long organizationId);

    List<Deviation> findAllByOrganizationIdAndCreatedAtBetween(
            Long organizationId,
            LocalDateTime from,
            LocalDateTime to);

    boolean existsByReportedBy(User user);
}
