package ntnu.no.fs_v26.repository;

import java.util.List;
import java.util.Optional;
import ntnu.no.fs_v26.model.Certification;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Certification} entities.
 *
 * <p>Provides queries for retrieving certifications earned by individual users
 * or across an entire organization, used in the training and compliance modules.
 */
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    /**
     * Returns all certifications earned by a specific user.
     *
     * @param user the user whose certifications to retrieve
     * @return a list of certifications for the given user
     */
    List<Certification> findByUser(User user);

    /**
     * Returns all certifications earned within a specific organization.
     *
     * @param organization the organization to retrieve certifications for
     * @return a list of certifications for the given organization
     */
    List<Certification> findByOrganization(Organization organization);

    /**
     * Returns the certification earned by a specific user for a specific training document,
     * if it exists.
     *
     * @param user     the user to look up
     * @param document the training document the certification relates to
     * @return an {@link Optional} containing the certification, or empty if not found
     */
    Optional<Certification> findByUserAndDocument(User user, TrainingDocument document);
}