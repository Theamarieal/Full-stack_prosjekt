package ntnu.no.fs_v26.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for applying rate limiting based on client IP addresses.
 *
 * <p>
 * This service stores a token bucket for each IP address and uses Bucket4j
 * to limit the number of requests allowed within a fixed time period.
 *
 * <p>
 * Each IP address is assigned its own bucket, allowing up to 10 requests
 * per minute.
 */
@Service
public class RateLimitingService {

    /**
     * Stores rate-limiting buckets per IP address.
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Resolves the bucket associated with the given IP address.
     *
     * <p>
     * If no bucket exists for the IP, a new one is created and stored.
     *
     * @param ip the client IP address
     * @return the rate-limiting bucket for the IP address
     */
    public Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, this::newBucket);
    }

    /**
     * Creates a new token bucket for a given IP address.
     *
     * <p>
     * The bucket allows a maximum capacity of 10 tokens and refills
     * 10 tokens every minute.
     *
     * @param ip the client IP address
     * @return a new configured bucket
     */
    private Bucket newBucket(String ip) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillIntervally(10, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}