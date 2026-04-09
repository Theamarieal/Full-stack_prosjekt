package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.Checklist;
import ntnu.no.fs_v26.model.Frequency;
import ntnu.no.fs_v26.model.ModuleType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    List<Checklist> findAllByOrganizationId(Long organizationId);

    @Query("SELECT c FROM Checklist c WHERE c.organization.id = :orgId " +
        "AND (:module IS NULL OR c.module = :module) " +
        "AND (:frequency IS NULL OR c.frequency = :frequency)")
    Page<Checklist> findFiltered(
        @Param("orgId") Long orgId,
        @Param("module") ModuleType module,
        @Param("frequency") Frequency frequency,
        Pageable pageable);
}