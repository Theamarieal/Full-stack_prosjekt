package ntnu.no.fs_v26.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service class for creating, parsing, and validating JSON Web Tokens (JWT).
 *
 * <p>
 * This service is responsible for generating signed JWTs, extracting claims
 * from tokens, and validating whether a token belongs to a given user and has
 * not expired.
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extracts the username (e-mail/subject) from a JWT.
     *
     * @param token the JWT
     * @return the username stored as the subject of the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generates a new JWT for the given user.
     *
     * @param userDetails the authenticated user's details
     * @return a signed JWT
     */
    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtExpiration);
    }

    /**
     * Validates a JWT against the provided user details.
     *
     * <p>
     * A token is considered valid if the username in the token matches
     * the user's username and the token has not expired.
     *
     * @param token       the JWT to validate
     * @param userDetails the user details to validate against
     * @return {@code true} if the token is valid, otherwise {@code false}
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Builds and signs a JWT with the given claims, subject, and expiration time.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details used as the token subject
     * @param expiration  the token expiration time in milliseconds
     * @return the signed JWT
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Checks whether a token has expired.
     *
     * @param token the JWT
     * @return {@code true} if the token is expired, otherwise {@code false}
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT.
     *
     * @param token the JWT
     * @return the expiration date of the token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT using the provided resolver function.
     *
     * @param token          the JWT
     * @param claimsResolver the function used to extract the desired claim
     * @param <T>            the type of the extracted claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from a signed JWT.
     *
     * @param token the JWT
     * @return the claims contained in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Decodes and returns the signing key used to sign and verify JWTs.
     *
     * @return the secret signing key
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}