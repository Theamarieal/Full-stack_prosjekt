package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.Deviation;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for {@link Deviation} entities.
 *
 * <p>Provides queries for retrieving deviation reports scoped to an organization,
 * with support for pagination and date range filtering used in report generation.
 */
@Repository
public interface DeviationRepository extends JpaRepository<Deviation, Long> {

    /**
     * Returns all deviations for a given organization.
     *
     * @param organizationId the ID of the organization
     * @return a list of all deviations for the organization
     */
    List<Deviation> findAllByOrganizationId(Long organizationId);

    /**
     * Returns a paginated list of deviations for a given organization.
     *
     * @param organizationId the ID of the organization
     * @param pageable       pagination and sorting parameters
     * @return a page of deviations for the organization
     */
    Page<Deviation> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /**
     * Returns all deviations for a given organization created within a timestamp range.
     * Used for report generation.
     *
     * @param organizationId the ID of the organization
     * @param from           the start of the time range (inclusive)
     * @param to             the end of the time range (inclusive)
     * @return a list of deviations within the given period
     */
    List<Deviation> findAllByOrganizationIdAndCreatedAtBetween(
        Long organizationId,
        LocalDateTime from,
        LocalDateTime to);

    /**
     * Checks whether any deviation has been reported by the given user.
     * Used to enforce referential integrity before deleting a user.
     *
     * @param user the user to check
     * @return {@code true} if the user has reported at least one deviation
     */
    boolean existsByReportedBy(User user);
}