package ntnu.no.fs_v26.repository;

import java.util.List;
import java.util.Optional;
import ntnu.no.fs_v26.model.DocumentAcknowledgement;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link DocumentAcknowledgement} entities.
 *
 * <p>Provides queries for tracking which users have acknowledged
 * which training documents, used in the training compliance module.
 */
public interface DocumentAcknowledgementRepository extends JpaRepository<DocumentAcknowledgement, Long> {

    /**
     * Returns the acknowledgement for a specific user and document, if it exists.
     * Used to prevent duplicate acknowledgements.
     *
     * @param user     the user to look up
     * @param document the training document to look up
     * @return an {@link Optional} containing the acknowledgement, or empty if not found
     */
    Optional<DocumentAcknowledgement> findByUserAndDocument(User user, TrainingDocument document);

    /**
     * Returns all acknowledgements for a specific training document.
     * Used by managers to track which employees have acknowledged a policy.
     *
     * @param document the training document to retrieve acknowledgements for
     * @return a list of acknowledgements for the given document
     */
    List<DocumentAcknowledgement> findByDocument(TrainingDocument document);

    /**
     * Returns all acknowledgements made by a specific user.
     *
     * @param user the user whose acknowledgements to retrieve
     * @return a list of acknowledgements for the given user
     */
    List<DocumentAcknowledgement> findByUser(User user);
}
