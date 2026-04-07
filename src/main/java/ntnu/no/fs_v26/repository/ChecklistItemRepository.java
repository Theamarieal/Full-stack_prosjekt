package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.ChecklistItem;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
  boolean existsByCompletedBy(User user);
}