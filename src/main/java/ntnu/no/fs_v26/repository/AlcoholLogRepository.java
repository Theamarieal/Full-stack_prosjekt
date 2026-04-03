package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.AlcoholLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for alcohol log persistence and filtering.
 */
public interface AlcoholLogRepository extends JpaRepository<AlcoholLog, Long> {

    /**
     * Returns all alcohol logs for a given organization and date ordered by
     * recorded time descending.
     *
     * @param organizationId the organization identifier
     * @param shiftDate      the date to retrieve logs for
     * @return a list of alcohol logs for the given date
     */
    List<AlcoholLog> findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
            Long organizationId,
            LocalDate shiftDate);
}