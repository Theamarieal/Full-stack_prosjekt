package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.AlcoholLog;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for {@link AlcoholLog} entities.
 *
 * <p>Provides queries for retrieving alcohol log entries scoped to an organization,
 * filtered by shift date or timestamp range, as part of the IK-Alkohol compliance module.
 */
@Repository
public interface AlcoholLogRepository extends JpaRepository<AlcoholLog, Long> {

    /**
     * Returns all alcohol log entries for a given organization and shift date,
     * ordered by recorded time descending.
     *
     * @param organizationId the ID of the organization
     * @param shiftDate      the date of the shift to retrieve logs for
     * @return a list of matching alcohol logs
     */
    List<AlcoholLog> findByOrganizationIdAndShiftDateOrderByRecordedAtDesc(
        Long organizationId,
        LocalDate shiftDate
    );

    /**
     * Returns all alcohol log entries for a given organization within a timestamp range,
     * ordered by recorded time descending.
     *
     * @param organizationId the ID of the organization
     * @param from           the start of the time range (inclusive)
     * @param to             the end of the time range (inclusive)
     * @return a list of matching alcohol logs
     */
    List<AlcoholLog> findByOrganizationIdAndRecordedAtBetweenOrderByRecordedAtDesc(
        Long organizationId,
        LocalDateTime from,
        LocalDateTime to
    );

    /**
     * Checks whether any alcohol logs have been recorded by the given user.
     * Used to enforce referential integrity before deleting a user.
     *
     * @param user the user to check
     * @return {@code true} if the user has recorded at least one alcohol log
     */
    boolean existsByRecordedBy(User user);
}