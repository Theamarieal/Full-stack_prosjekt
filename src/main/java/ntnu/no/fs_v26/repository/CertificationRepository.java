package ntnu.no.fs_v26.repository;

import java.util.List;
import java.util.Optional;
import ntnu.no.fs_v26.model.Certification;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByUser(User user);

    List<Certification> findByOrganization(Organization organization);

    Optional<Certification> findByUserAndDocument(User user, TrainingDocument document);
}