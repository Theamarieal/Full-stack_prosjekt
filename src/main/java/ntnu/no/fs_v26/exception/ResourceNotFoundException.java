package ntnu.no.fs_v26.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * <p>This exception is typically used when an entity with a given identifier
 * does not exist, such as when querying a user, document, or record that is not present.</p>
 *
 * <p>It is handled by the global exception handler and results in an HTTP 404 Not Found response.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code ResourceNotFoundException} with the specified detail message.
     *
     * @param message a descriptive message explaining which resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}