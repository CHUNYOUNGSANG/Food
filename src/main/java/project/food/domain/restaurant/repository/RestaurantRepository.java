package project.food.domain.restaurant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.food.domain.restaurant.entity.Restaurant;

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
}