package ntnu.no.fs_v26.repository;

import java.util.List;
import java.util.Optional;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.Role;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link User} entities.
 *
 * <p>Provides queries for looking up users by email and filtering by organization
 * and role, supporting authentication, admin management, and multi-tenant data scoping.
 */
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Returns the user with the given email address, if one exists.
   * Used during authentication and to check for duplicate registrations.
   *
   * @param email the email address to look up
   * @return an {@link Optional} containing the user, or empty if not found
   */
  Optional<User> findByEmail(String email);

  /**
   * Returns all users in a given organization with a specific role.
   *
   * @param organization the organization to filter by
   * @param role         the role to filter by
   * @return a list of users matching the given organization and role
   */
  List<User> findByOrganizationAndRole(Organization organization, Role role);

  /**
   * Returns all users in a given organization with any of the specified roles.
   * Used to retrieve employees and managers together in a single query.
   *
   * @param organization the organization to filter by
   * @param roles        the list of roles to include
   * @return a list of users matching the given organization and any of the given roles
   */
  List<User> findByOrganizationAndRoleIn(Organization organization, List<Role> roles);

  /**
   * Returns all users belonging to a specific organization.
   *
   * @param organization the organization to retrieve users for
   * @return a list of all users in the given organization
   */
  List<User> findByOrganization(Organization organization);
}
