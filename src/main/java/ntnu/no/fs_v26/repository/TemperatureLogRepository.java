package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.TemperatureLog;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for {@link TemperatureLog} entities.
 *
 * <p>Provides queries for retrieving and counting temperature log entries
 * scoped to an organization, used in the IK-Mat food safety compliance module.
 * Includes support for deviation filtering and dashboard summary generation.
 */
@Repository
public interface TemperatureLogRepository extends JpaRepository<TemperatureLog, Long> {

    /**
     * Returns all temperature logs for equipment belonging to a specific organization.
     *
     * @param organizationId the ID of the organization
     * @return a list of all temperature logs for the organization
     */
    List<TemperatureLog> findAllByEquipmentOrganizationId(Long organizationId);

    /**
     * Returns all temperature logs for a given organization within a timestamp range.
     * Used for filtered history views and report generation.
     *
     * @param organizationId the ID of the organization
     * @param from           the start of the time range (inclusive)
     * @param to             the end of the time range (inclusive)
     * @return a list of temperature logs within the given period
     */
    List<TemperatureLog> findAllByEquipmentOrganizationIdAndTimestampBetween(
        Long organizationId,
        LocalDateTime from,
        LocalDateTime to);

    /**
     * Returns the 10 most recent temperature logs for a given organization.
     * Used for the latest-readings view in the temperature dashboard.
     *
     * @param organizationId the ID of the organization
     * @return a list of up to 10 recent temperature logs
     */
    List<TemperatureLog> findTop10ByEquipmentOrganizationIdOrderByTimestampDesc(Long organizationId);

    /**
     * Returns the 5 most recent temperature deviation entries for a given organization.
     * Used by the dashboard to highlight recent out-of-range readings.
     *
     * @param organizationId the ID of the organization
     * @return a list of up to 5 recent temperature logs flagged as deviations
     */
    List<TemperatureLog> findTop5ByEquipmentOrganizationIdAndIsDeviationTrueOrderByTimestampDesc(Long organizationId);

    /**
     * Counts all temperature logs for a given organization within a timestamp range.
     *
     * @param organizationId the ID of the organization
     * @param start          the start of the time range (inclusive)
     * @param end            the end of the time range (inclusive)
     * @return the total number of temperature logs in the period
     */
    long countByEquipmentOrganizationIdAndTimestampBetween(
        Long organizationId,
        LocalDateTime start,
        LocalDateTime end);

    /**
     * Counts temperature deviation entries for a given organization within a timestamp range.
     * Used for generating summary statistics in the dashboard and reports.
     *
     * @param organizationId the ID of the organization
     * @param start          the start of the time range (inclusive)
     * @param end            the end of the time range (inclusive)
     * @return the number of temperature logs flagged as deviations in the period
     */
    long countByEquipmentOrganizationIdAndIsDeviationTrueAndTimestampBetween(
        Long organizationId,
        LocalDateTime start,
        LocalDateTime end);

    /**
     * Checks whether any temperature log has been recorded by the given user.
     * Used to enforce referential integrity before deleting a user.
     *
     * @param user the user to check
     * @return {@code true} if the user has recorded at least one temperature log
     */
    boolean existsByLoggedBy(User user);
}