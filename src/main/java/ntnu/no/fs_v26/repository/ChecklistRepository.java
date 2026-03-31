package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
}