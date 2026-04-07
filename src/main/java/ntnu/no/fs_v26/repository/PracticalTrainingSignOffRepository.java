package ntnu.no.fs_v26.repository;

import java.util.List;
import java.util.Optional;
import ntnu.no.fs_v26.model.PracticalTrainingSignOff;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PracticalTrainingSignOffRepository extends JpaRepository<PracticalTrainingSignOff, Long> {
    Optional<PracticalTrainingSignOff> findByEmployeeAndDocument(User employee, TrainingDocument document);

    List<PracticalTrainingSignOff> findByDocument(TrainingDocument document);

    List<PracticalTrainingSignOff> findByEmployee(User employee);
}