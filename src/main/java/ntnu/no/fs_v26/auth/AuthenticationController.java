package ntnu.no.fs_v26.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ntnu.no.fs_v26.security.RateLimitingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication endpoints.
 *
 * <p>Handles user registration and login. All endpoints are public and do not
 * require authentication. Rate limiting is applied to the login endpoint
 * to mitigate brute-force attacks.
 *
 * <p>Base path: {@code /api/v1/auth}
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthenticationController {

    private final AuthenticationService service;
    private final RateLimitingService rateLimitingService;

    /**
     * Registers a new user account and returns a JWT token.
     *
     * @param request the registration request containing email, password, role, and organization ID
     * @return a {@link ResponseEntity} containing the JWT token and basic user info
     */
    @Operation(summary = "Register a new user", description = "Creates a new user and returns a JWT token")
    @ApiResponse(responseCode = "200", description = "User created successfully")
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
        @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * <p>Rate limiting is enforced per client IP address. If the limit is exceeded,
     * HTTP 429 Too Many Requests is returned.
     *
     * @param request     the login request containing email and password
     * @param httpRequest the HTTP servlet request used to extract the client IP address
     * @return a {@link ResponseEntity} containing the JWT token and basic user info,
     *         or HTTP 429 if the rate limit has been exceeded
     */
    @Operation(summary = "Authenticate user", description = "Logs in a user and returns a JWT token")
    @ApiResponse(responseCode = "200", description = "Successfully authenticated")
    @ApiResponse(responseCode = "429", description = "Too many login attempts. Please try again in a minute.")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
        @Valid @RequestBody AuthenticationRequest request,
        HttpServletRequest httpRequest) {

        String clientIp = httpRequest.getRemoteAddr();

        if (!rateLimitingService.resolveBucket(clientIp).tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        return ResponseEntity.ok(service.authenticate(request));
    }
}