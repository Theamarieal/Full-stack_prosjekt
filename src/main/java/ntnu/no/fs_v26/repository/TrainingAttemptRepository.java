package ntnu.no.fs_v26.repository;

import java.util.List;
import java.util.Optional;
import ntnu.no.fs_v26.model.TrainingAttempt;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingAttemptRepository extends JpaRepository<TrainingAttempt, Long> {
    List<TrainingAttempt> findByUser(User user);

    Optional<TrainingAttempt> findTopByUserAndDocumentOrderByCompletedAtDesc(User user, TrainingDocument document);
}