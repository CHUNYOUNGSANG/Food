package project.food.domain.restaurant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.food.domain.restaurant.entity.Restaurant;

import java.util.List;
import java.util.Optional;

/**
 * 맛집 Repository
 * - 맛집 CRUD 및 검색 쿼리 담당
 */
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // sourceId(카카오 place id) 중복 체크
    boolean existsBySourceId(String sourceId);

    // sourceId로 맛집 조회
    Optional<Restaurant> findBySourceId(String sourceId);

    /**
     * 통합 검색 (이름/주소/카테고리 부분 일치)
     * - q가 null이거나 빈 문자열이면 전체 조회
     * - q가 있으면 이름, 주소, 카테고리에서 LIKE 검색
     */
    @Query("""
        SELECT r FROM Restaurant r
        WHERE :q IS NULL OR :q = ''
           OR r.name LIKE CONCAT('%', :q, '%')
           OR r.address LIKE CONCAT('%', :q, '%')
           OR r.category LIKE CONCAT('%', :q, '%')
        """)
    Page<Restaurant> search(String q, Pageable pageable);

    // ─── 추천맛집: 첫 페이지 (평균 평점 높은 순) ─────────────────────────
    @Query(nativeQuery = true, value = """
        SELECT r.id, r.name, r.address, r.category,
               AVG(p.rating) AS avg_rating
        FROM restaurant r
        INNER JOIN post p ON p.restaurant_id = r.id
        WHERE p.rating IS NOT NULL
        GROUP BY r.id, r.name, r.address, r.category
        ORDER BY avg_rating DESC, r.id DESC
        LIMIT :size
        """)
    List<Object[]> findRecommended(@Param("size") int size);

    // ─── 추천맛집: cursor 이후 페이지 ────────────────────────────────────
    @Query(nativeQuery = true, value = """
        SELECT r.id, r.name, r.address, r.category,
               AVG(p.rating) AS avg_rating
        FROM restaurant r
        INNER JOIN post p ON p.restaurant_id = r.id
        WHERE p.rating IS NOT NULL
        GROUP BY r.id, r.name, r.address, r.category
        HAVING AVG(p.rating) < :lastRating
            OR (AVG(p.rating) = :lastRating AND r.id < :lastId)
        ORDER BY avg_rating DESC, r.id DESC
        LIMIT :size
        """)
    List<Object[]> findRecommendedAfterCursor(
            @Param("lastRating") Double lastRating,
            @Param("lastId") Long lastId,
            @Param("size") int size
    );

    // ─── 인기맛집: 첫 페이지 (리뷰 수 많은 순) ───────────────────────────
    @Query(nativeQuery = true, value = """
        SELECT r.id, r.name, r.address, r.category,
               COUNT(p.id) AS post_count
        FROM restaurant r
        LEFT JOIN post p ON p.restaurant_id = r.id
        GROUP BY r.id, r.name, r.address, r.category
        ORDER BY post_count DESC, r.id DESC
        LIMIT :size
        """)
    List<Object[]> findPopular(@Param("size") int size);

    // ─── 인기맛집: cursor 이후 페이지 ────────────────────────────────────
    @Query(nativeQuery = true, value = """
        SELECT r.id, r.name, r.address, r.category,
               COUNT(p.id) AS post_count
        FROM restaurant r
        LEFT JOIN post p ON p.restaurant_id = r.id
        GROUP BY r.id, r.name, r.address, r.category
        HAVING COUNT(p.id) < :lastCount
            OR (COUNT(p.id) = :lastCount AND r.id < :lastId)
        ORDER BY post_count DESC, r.id DESC
        LIMIT :size
        """)
    List<Object[]> findPopularAfterCursor(
            @Param("lastCount") Long lastCount,
            @Param("lastId") Long lastId,
            @Param("size") int size
    );
}