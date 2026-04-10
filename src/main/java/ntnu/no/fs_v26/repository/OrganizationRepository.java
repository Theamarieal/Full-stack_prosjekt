package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link Organization} entities.
 *
 * <p>Provides standard CRUD operations for organizations.
 * Organizations represent tenants in the multi-tenant architecture of Checkd,
 * and all user and compliance data is scoped to a specific organization.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}