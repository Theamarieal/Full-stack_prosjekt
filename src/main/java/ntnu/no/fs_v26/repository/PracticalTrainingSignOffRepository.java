package ntnu.no.fs_v26.repository;

import java.util.List;
import java.util.Optional;
import ntnu.no.fs_v26.model.PracticalTrainingSignOff;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link PracticalTrainingSignOff} entities.
 *
 * <p>Provides queries for tracking practical training sign-offs,
 * where a manager approves that an employee has completed hands-on training
 * for a specific document.
 */
public interface PracticalTrainingSignOffRepository extends JpaRepository<PracticalTrainingSignOff, Long> {

    /**
     * Returns the practical sign-off for a specific employee and document, if it exists.
     * Used to prevent duplicate sign-offs.
     *
     * @param employee the employee to look up
     * @param document the training document to look up
     * @return an {@link Optional} containing the sign-off, or empty if not found
     */
    Optional<PracticalTrainingSignOff> findByEmployeeAndDocument(User employee, TrainingDocument document);

    /**
     * Returns all practical sign-offs for a specific training document.
     * Used by managers to track which employees have completed practical training.
     *
     * @param document the training document to retrieve sign-offs for
     * @return a list of sign-offs for the given document
     */
    List<PracticalTrainingSignOff> findByDocument(TrainingDocument document);

    /**
     * Returns all practical sign-offs for a specific employee.
     *
     * @param employee the employee whose sign-offs to retrieve
     * @return a list of sign-offs for the given employee
     */
    List<PracticalTrainingSignOff> findByEmployee(User employee);
}