package ntnu.no.fs_v26.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthenticationController {

  private final AuthenticationService service;

  @Operation(summary = "Register a new user", description = "Creates a new user and returns a JWT token")
  @ApiResponse(responseCode = "200", description = "User created successfully")
  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(
      @RequestBody RegisterRequest request
  ) {
    return ResponseEntity.ok(service.register(request));
  }

  @Operation(summary = "Authenticate user", description = "Logs in a user and returns a JWT token")
  @ApiResponse(responseCode = "200", description = "Successfully authenticated")
  @ApiResponse(responseCode = "403", description = "Invalid credentials")
  @PostMapping("/login") // Endret fra /authenticate for å matche Issue #19 nøyaktig
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody AuthenticationRequest request
  ) {
    return ResponseEntity.ok(service.authenticate(request));
  }
}