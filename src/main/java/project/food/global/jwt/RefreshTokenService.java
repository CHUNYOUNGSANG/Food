package project.food.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private static final String KEY_PREFIX = "refresh:token:";

    public void save(Long memberId, String refreshToken) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + memberId,
                refreshToken,
                Duration.ofMillis(refreshTokenExpiration)
        );
    }

    public boolean isValid(Long memberId, String refreshToken) {
        String stored = redisTemplate.opsForValue().get(KEY_PREFIX + memberId);
        return refreshToken.equals(stored);
    }

    public void delete(Long memberId) {
        redisTemplate.delete(KEY_PREFIX + memberId);
    }
}