package ntnu.no.fs_v26.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler for all REST controllers.
 *
 * <p>Catches validation errors thrown by {@code @Valid} and returns a
 * structured 400 Bad Request response instead of the default Spring error page.
 * This satisfies the OWASP requirement for predictable, safe error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors from {@code @Valid} on request bodies.
     *
     * <p>Triggered when a field annotated with {@code @NotBlank}, {@code @Size},
     * {@code @Email}, etc. fails its constraint. Returns HTTP 400 with a list
     * of all failing fields and their messages.
     *
     * <p>Example response body:
     * <pre>
     * {
     *   "status": 400,
     *   "error": "Validation failed",
     *   "timestamp": "2026-04-06T12:00:00",
     *   "errors": [
     *     "email: Email must be a valid email address",
     *     "password: Password is required"
     *   ]
     * }
     * </pre>
     *
     * @param ex the validation exception thrown by Spring
     * @return a 400 response with field-level error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        // Collect all field errors into readable "field: message" strings
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        // Build a structured error response body
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation failed");
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles illegal argument errors thrown by service layer logic.
     *
     * <p>Used when business rules are violated — for example, trying to update
     * a deviation that belongs to a different organization.
     *
     * @param ex the exception thrown by service layer
     * @return a 400 response with the error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", ex.getMessage());
        body.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 404);
        body.put("error", ex.getMessage());
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(404).body(body);
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ResourceConflictException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 409);
        body.put("error", ex.getMessage());
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(409).body(body);
    }
}
