package ntnu.no.fs_v26.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {
    // saves a "bucket" for each IP-adress
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, this::newBucket);
    }

    private Bucket newBucket(String ip) {
        // Modern Bucket4j 8.x+ syntax:
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10) // max 10 tokens in the bucket
                        .refillIntervally(10, Duration.ofMinutes(1)) // fill 10 new for each minute
                        .build())
                .build();
    }
}