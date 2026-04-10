package ntnu.no.fs_v26.exception;

/**
 * Exception thrown when a request results in a resource conflict.
 *
 * <p>This exception is typically used when attempting to create or modify a resource
 * in a way that violates application constraints, such as creating a duplicate entry
 * or conflicting with existing data.</p>
 *
 * <p>It is handled by the global exception handler and results in an HTTP 409 Conflict response.</p>
 */
public class ResourceConflictException extends RuntimeException {

    /**
     * Constructs a new {@code ResourceConflictException} with the specified detail message.
     *
     * @param message a descriptive message explaining the conflict
     */
    public ResourceConflictException(String message) {
        super(message);
    }
}