package ntnu.no.fs_v26.repository;

import java.util.List;
import java.util.Optional;
import ntnu.no.fs_v26.model.Organization;
import ntnu.no.fs_v26.model.Role;
import ntnu.no.fs_v26.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

  List<User> findByOrganizationAndRole(Organization organization, Role role);
  List<User> findByOrganizationAndRoleIn(Organization organization, List<Role> roles);
  List<User> findByOrganization(Organization organization);
}
