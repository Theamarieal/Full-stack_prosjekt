package ntnu.no.fs_v26.repository;

import ntnu.no.fs_v26.model.ChecklistItem;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link ChecklistItem} entities.
 *
 * <p>Provides queries for individual checklist item records,
 * including checks for user activity used before account deletion.
 */
@Repository
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

  /**
   * Checks whether any checklist item has been completed by the given user.
   * Used to enforce referential integrity before deleting a user.
   *
   * @param user the user to check
   * @return {@code true} if the user has completed at least one checklist item
   */
  boolean existsByCompletedBy(User user);
}