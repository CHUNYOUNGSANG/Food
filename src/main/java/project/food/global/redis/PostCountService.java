package project.food.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCountService {

    private final StringRedisTemplate redisTemplate;

    private static final String VIEW_KEY = "view:count:";
    private static final String LIKE_KEY = "like:count:";

    // ── 조회수 ──────────────────────────────────────────

    /** 조회수 1 증가 후 반환 */
    public long incrementViewCount(Long postId) {
        Long count = redisTemplate.opsForValue().increment(VIEW_KEY + postId);
        return count != null ? count : 0L;
    }

    /** 조회수 반환 (없으면 DB 값으로 초기화) */
    public long getViewCount(Long postId, int dbViewCount) {
        String value = redisTemplate.opsForValue().get(VIEW_KEY + postId);
        if (value != null) return Long.parseLong(value);
        // Redis에 없으면 DB 값으로 초기화
        redisTemplate.opsForValue().set(VIEW_KEY + postId, String.valueOf(dbViewCount));
        return dbViewCount;
    }

    /** 게시글 삭제 시 Redis 키 제거 */
    public void deleteViewCount(Long postId) {
        redisTemplate.delete(VIEW_KEY + postId);
    }

    // ── 좋아요 수 ────────────────────────────────────────

    /** 좋아요 수 1 증가 */
    public long incrementLikeCount(Long postId) {
        Long count = redisTemplate.opsForValue().increment(LIKE_KEY + postId);
        return count != null ? count : 0L;
    }

    /** 좋아요 수 1 감소 (최소 0) */
    public long decrementLikeCount(Long postId) {
        Long count = redisTemplate.opsForValue().decrement(LIKE_KEY + postId);
        if (count != null && count < 0) {
            redisTemplate.opsForValue().set(LIKE_KEY + postId, "0");
            return 0L;
        }
        return count != null ? count : 0L;
    }

    /** 좋아요 수 반환 (없으면 DB 값으로 초기화) */
    public long getLikeCount(Long postId, long dbLikeCount) {
        String value = redisTemplate.opsForValue().get(LIKE_KEY + postId);
        if (value != null) return Long.parseLong(value);
        redisTemplate.opsForValue().set(LIKE_KEY + postId, String.valueOf(dbLikeCount));
        return dbLikeCount;
    }

    /** 게시글 삭제 시 Redis 키 제거 */
    public void deleteLikeCount(Long postId) {
        redisTemplate.delete(LIKE_KEY + postId);
    }
}