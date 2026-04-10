package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link Equipment} entities.
 *
 * <p>Provides queries for retrieving equipment registered to an organization,
 * used in the IK-Mat temperature logging module.
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    /**
     * Returns all equipment belonging to a specific organization, ordered by name ascending.
     *
     * @param organizationId the ID of the organization
     * @return an alphabetically sorted list of equipment for the organization
     */
    List<Equipment> findAllByOrganizationIdOrderByNameAsc(Long organizationId);
}