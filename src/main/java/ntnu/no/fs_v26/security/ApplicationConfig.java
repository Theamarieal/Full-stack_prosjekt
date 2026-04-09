package ntnu.no.fs_v26.security;

import ntnu.no.fs_v26.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application security configuration class.
 *
 * <p>
 * This class defines Spring beans related to authentication, including
 * the {@link UserDetailsService}, {@link AuthenticationProvider},
 * {@link AuthenticationManager}, and {@link PasswordEncoder}.
 *
 * <p>
 * It uses the {@link UserRepository} to load users by e-mail address
 * during authentication.
 */
@Configuration
public class ApplicationConfig {

  private final UserRepository userRepository;

  /**
   * Creates a new application configuration instance.
   *
   * @param userRepository the repository used to retrieve users from the database
   */
  public ApplicationConfig(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Creates a {@link UserDetailsService} bean that loads a user by e-mail.
   *
   * @return a user details service implementation
   * @throws UsernameNotFoundException if no user exists with the given e-mail
   */
  @Bean
  public UserDetailsService userDetailsService() {
    return username -> userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with e-mail: " + username));
  }

  /**
   * Creates an {@link AuthenticationProvider} bean based on DAO authentication.
   *
   * <p>
   * The provider uses the configured {@link UserDetailsService} and
   * {@link PasswordEncoder} to authenticate users.
   *
   * @return the authentication provider
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService());
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * Creates the {@link AuthenticationManager} bean used by Spring Security.
   *
   * @param config the authentication configuration provided by Spring
   * @return the authentication manager
   * @throws Exception if the authentication manager cannot be created
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Creates the password encoder bean used to hash and verify passwords.
   *
   * @return a BCrypt password encoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}