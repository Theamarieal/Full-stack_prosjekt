package ntnu.no.fs_v26;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Checkd Spring Boot application.
 *
 * <p>Bootstraps the application context and starts the embedded web server.
 */
@SpringBootApplication
public class FsV26Application {

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(FsV26Application.class, args);
	}
}
