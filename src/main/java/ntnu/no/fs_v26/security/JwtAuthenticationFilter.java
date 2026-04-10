package ntnu.no.fs_v26.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter that processes incoming HTTP requests.
 *
 * <p>
 * This filter checks for a JWT in the {@code Authorization} header,
 * validates the token, and sets the authenticated user in the
 * {@link SecurityContextHolder} if the token is valid.
 *
 * <p>
 * The filter is executed once per request and skips authentication
 * for authentication endpoints.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  /**
   * Determines whether the current request should bypass this filter.
   *
   * <p>
   * Requests targeting authentication endpoints are excluded from JWT filtering.
   *
   * @param request the current HTTP request
   * @return {@code true} if the request should not be filtered, otherwise
   *         {@code false}
   * @throws ServletException if an error occurs while checking the request
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    return path.contains("/api/v1/auth");
  }

  /**
   * Performs the internal filtering logic for JWT authentication.
   *
   * <p>
   * If the request contains a valid bearer token, the user is authenticated
   * and stored in the Spring Security context. If the token is missing or
   * invalid,
   * the request continues without authentication or returns
   * {@code 401 Unauthorized}
   * if token parsing fails.
   *
   * @param request     the incoming HTTP request
   * @param response    the outgoing HTTP response
   * @param filterChain the filter chain
   * @throws ServletException if a servlet-related error occurs
   * @throws IOException      if an I/O error occurs
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userEmail;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      jwt = authHeader.substring(7);
      userEmail = jwtService.extractUsername(jwt);
    } catch (Exception e) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
      if (jwtService.isTokenValid(jwt, userDetails)) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }
    filterChain.doFilter(request, response);
  }
}