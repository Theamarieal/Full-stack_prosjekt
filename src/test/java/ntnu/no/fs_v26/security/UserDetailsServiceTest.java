package ntnu.no.fs_v26.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import ntnu.no.fs_v26.model.User;
import ntnu.no.fs_v26.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ApplicationConfig applicationConfig;

  @Test
  void shouldLoadUserByUsername() {
    // Arrange
    String email = "test@ntnu.no";
    User user = new User();
    user.setEmail(email);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    // Act
    UserDetails result = applicationConfig.userDetailsService().loadUserByUsername(email);

    // Assert
    assertEquals(email, result.getUsername());
    verify(userRepository, times(1)).findByEmail(email);
  }

  @Test
  void shouldThrowExceptionWhenUserNotFound() {
    // Arrange
    String email = "unexisting@ntnu.no";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UsernameNotFoundException.class, () -> {
      applicationConfig.userDetailsService().loadUserByUsername(email);
    });
  }
}