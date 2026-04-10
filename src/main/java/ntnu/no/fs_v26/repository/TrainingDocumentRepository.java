package ntnu.no.fs_v26.repository;

import java.util.List;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.TrainingDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link TrainingDocument} entities.
 *
 * <p>Provides queries for retrieving active training documents scoped to an organization,
 * with optional filtering by document type (POLICY or TRAINING).
 */
public interface TrainingDocumentRepository extends JpaRepository<TrainingDocument, Long> {

    /**
     * Returns all active training documents for a given organization.
     *
     * @param organization the organization to retrieve documents for
     * @return a list of active training documents for the organization
     */
    List<TrainingDocument> findByOrganizationAndActiveTrue(Organization organization);

    /**
     * Returns all active training documents of a specific type for a given organization.
     *
     * @param organization the organization to retrieve documents for
     * @param type         the document type to filter by (e.g. POLICY, TRAINING)
     * @return a list of active training documents matching the given type
     */
    List<TrainingDocument> findByOrganizationAndTypeAndActiveTrue(
        Organization organization,
        TrainingDocumentType type);
}