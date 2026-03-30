package project.food.domain.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import project.food.global.common.CachedPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.post.repository.PostRepository;
import project.food.domain.restaurant.dto.RestaurantDetailResponse;
import project.food.domain.restaurant.dto.RestaurantListItemResponse;
import project.food.domain.restaurant.dto.RestaurantRankResponse;
import project.food.domain.restaurant.dto.RestaurantReviewItemResponse;
import project.food.domain.restaurant.entity.Restaurant;
import project.food.domain.restaurant.repository.RestaurantRepository;
import project.food.global.common.CursorPageResponse;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * 맛집 조회 서비스
 * - 목록 조회 (통합 검색 지원)
 * - 상세 조회
 * - 맛집별 리뷰(게시글) 목록 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final PostRepository postRepository;

    /**
     * 맛집 목록 조회 (통합 검색)
     * - q가 비어있으면 전체 목록 반환
     * - q가 있으면 이름/주소/카테고리에서 부분 일치 검색
     *
     * @param q    검색어 (이름, 주소, 카테고리 통합 검색)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 맛집 목록 (간략 정보)
     */
    @Cacheable(value = "restaurants", key = "(#q ?: 'all') + '-' + #page + '-' + #size")
    public CachedPage<RestaurantListItemResponse> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        // 검색어가 없으면 전체 조회, 있으면 이름/주소/카테고리 통합 검색
        Page<RestaurantListItemResponse> result = ((q == null || q.isBlank())
                ? restaurantRepository.findAll(pageable)
                : restaurantRepository.search(q, pageable))
                .map(RestaurantListItemResponse::from);

        return new CachedPage<>(result.getContent(), page, size, result.getTotalElements());
    }

    /**
     * 맛집 상세 조회
     * - 맛집 기본 정보 + 해당 맛집의 리뷰 목록을 함께 반환
     *
     * @param restaurantId 맛집 ID
     * @return 맛집 상세 정보 (리뷰 포함)
     */
    @Cacheable(value = "restaurant", key = "#restaurantId")
    public RestaurantDetailResponse getDetail(Long restaurantId) {
        // 맛집 조회
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESTAURANT_NOT_FOUND));

        // 해당 맛집의 리뷰(게시글) 최신순 조회
        Page<RestaurantReviewItemResponse> reviews = postRepository
                .findByRestaurant_IdOrderByCreatedAtDesc(restaurantId, PageRequest.of(0, 10))
                .map(RestaurantReviewItemResponse::from);

        return RestaurantDetailResponse.of(restaurant, reviews);
    }

    /**
     * 맛집별 리뷰(게시글) 목록 조회
     * - 특정 맛집에 작성된 리뷰를 페이지네이션으로 조회
     *
     * @param restaurantId 맛집 ID
     * @param page         페이지 번호
     * @param size         페이지 크기
     * @return 리뷰 목록
     */
    public Page<RestaurantReviewItemResponse> getRestaurantPosts(Long restaurantId, int page, int size) {
        // 맛집 존재 여부 확인
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new CustomException(ErrorCode.RESTAURANT_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return postRepository
                .findByRestaurant_IdOrderByCreatedAtDesc(restaurantId, pageable)
                .map(RestaurantReviewItemResponse::from);
    }

    /**
     * 추천맛집 목록 (평균 평점 높은 순, cursor 기반 무한 스크롤)
     *
     * @param cursor Base64 인코딩된 "avgRating,id" (첫 페이지는 null)
     * @param size   한 페이지 크기
     * @return cursor 페이지 응답
     */
    @Cacheable(value = "restaurants-recommended", key = "(#cursor ?: 'first') + '-' + #size")
    public CursorPageResponse<RestaurantRankResponse> getRecommended(String cursor, int size) {
        List<Object[]> rows;

        if (cursor == null || cursor.isBlank()) {
            rows = restaurantRepository.findRecommended(size + 1);
        } else {
            String[] parts = decodeCursor(cursor);
            Double lastRating = Double.parseDouble(parts[0]);
            Long lastId = Long.parseLong(parts[1]);
            rows = restaurantRepository.findRecommendedAfterCursor(lastRating, lastId, size + 1);
        }

        boolean hasNext = rows.size() > size;
        List<Object[]> pageRows = hasNext ? rows.subList(0, size) : rows;

        List<RestaurantRankResponse> content = pageRows.stream()
                .map(row -> RestaurantRankResponse.builder()
                        .id(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .address((String) row[2])
                        .category((String) row[3])
                        .avgRating(((Number) row[4]).doubleValue())
                        .build())
                .toList();

        String nextCursor = null;
        if (hasNext) {
            RestaurantRankResponse last = content.get(content.size() - 1);
            nextCursor = encodeCursor(last.getAvgRating().toString(), last.getId());
        }

        return CursorPageResponse.<RestaurantRankResponse>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 인기맛집 목록 (리뷰 수 많은 순, cursor 기반 무한 스크롤)
     *
     * @param cursor Base64 인코딩된 "postCount,id" (첫 페이지는 null)
     * @param size   한 페이지 크기
     * @return cursor 페이지 응답
     */
    @Cacheable(value = "restaurants-popular", key = "(#cursor ?: 'first') + '-' + #size")
    public CursorPageResponse<RestaurantRankResponse> getPopular(String cursor, int size) {
        List<Object[]> rows;

        if (cursor == null || cursor.isBlank()) {
            rows = restaurantRepository.findPopular(size + 1);
        } else {
            String[] parts = decodeCursor(cursor);
            Long lastCount = Long.parseLong(parts[0]);
            Long lastId = Long.parseLong(parts[1]);
            rows = restaurantRepository.findPopularAfterCursor(lastCount, lastId, size + 1);
        }

        boolean hasNext = rows.size() > size;
        List<Object[]> pageRows = hasNext ? rows.subList(0, size) : rows;

        List<RestaurantRankResponse> content = pageRows.stream()
                .map(row -> RestaurantRankResponse.builder()
                        .id(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .address((String) row[2])
                        .category((String) row[3])
                        .postCount(((Number) row[4]).longValue())
                        .build())
                .toList();

        String nextCursor = null;
        if (hasNext) {
            RestaurantRankResponse last = content.get(content.size() - 1);
            nextCursor = encodeCursor(last.getPostCount().toString(), last.getId());
        }

        return CursorPageResponse.<RestaurantRankResponse>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    private String encodeCursor(String sortValue, Long id) {
        String raw = sortValue + "," + id;
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String[] decodeCursor(String cursor) {
        byte[] decoded = Base64.getDecoder().decode(cursor);
        return new String(decoded, StandardCharsets.UTF_8).split(",", 2);
    }
}