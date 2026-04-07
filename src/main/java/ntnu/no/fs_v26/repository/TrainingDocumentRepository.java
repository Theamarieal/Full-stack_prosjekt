package ntnu.no.fs_v26.repository;

import java.util.List;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.TrainingDocument;
import ntnu.no.fs_v26.model.TrainingDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingDocumentRepository extends JpaRepository<TrainingDocument, Long> {
    List<TrainingDocument> findByOrganizationAndActiveTrue(Organization organization);

    List<TrainingDocument> findByOrganizationAndTypeAndActiveTrue(
            Organization organization,
            TrainingDocumentType type);
}