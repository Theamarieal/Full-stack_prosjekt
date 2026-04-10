package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.Checklist;
import ntnu.no.fs_v26.model.Frequency;
import ntnu.no.fs_v26.model.ModuleType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Checklist} entities.
 *
 * <p>Provides queries for retrieving checklists scoped to an organization,
 * with support for filtering by module type and frequency.
 */
@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    /**
     * Returns all checklists belonging to a specific organization.
     *
     * @param organizationId the ID of the organization
     * @return a list of all checklists for the given organization
     */
    List<Checklist> findAllByOrganizationId(Long organizationId);

    /**
     * Returns a paginated list of checklists for a given organization,
     * with optional filtering by module type and frequency.
     * Null values for {@code module} or {@code frequency} are treated as "no filter".
     *
     * @param orgId     the ID of the organization
     * @param module    optional filter by module type (e.g. KITCHEN, BAR); null to skip
     * @param frequency optional filter by frequency (e.g. DAILY, WEEKLY); null to skip
     * @param pageable  pagination and sorting parameters
     * @return a page of matching checklists
     */
    @Query("SELECT c FROM Checklist c WHERE c.organization.id = :orgId " +
        "AND (:module IS NULL OR c.module = :module) " +
        "AND (:frequency IS NULL OR c.frequency = :frequency)")
    Page<Checklist> findFiltered(
        @Param("orgId") Long orgId,
        @Param("module") ModuleType module,
        @Param("frequency") Frequency frequency,
        Pageable pageable);
}