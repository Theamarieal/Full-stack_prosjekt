package ntnu.no.fs_v26.repository;

import java.util.List;
import java.util.Optional;
import ntnu.no.fs_v26.model.DocumentAcknowledgement;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAcknowledgementRepository extends JpaRepository<DocumentAcknowledgement, Long> {
    Optional<DocumentAcknowledgement> findByUserAndDocument(User user, TrainingDocument document);

    List<DocumentAcknowledgement> findByDocument(TrainingDocument document);

    List<DocumentAcknowledgement> findByUser(User user);
}
