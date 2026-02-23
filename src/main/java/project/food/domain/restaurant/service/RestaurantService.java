package project.food.domain.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.post.repository.PostRepository;
import project.food.domain.restaurant.dto.RestaurantDetailResponse;
import project.food.domain.restaurant.dto.RestaurantListItemResponse;
import project.food.domain.restaurant.dto.RestaurantReviewItemResponse;
import project.food.domain.restaurant.entity.Restaurant;
import project.food.domain.restaurant.repository.RestaurantRepository;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;

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
    public Page<RestaurantListItemResponse> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        // 검색어가 없으면 전체 조회, 있으면 이름/주소/카테고리 통합 검색
        Page<Restaurant> result = (q == null || q.isBlank())
                ? restaurantRepository.findAll(pageable)
                : restaurantRepository.search(q, pageable);

        return result.map(RestaurantListItemResponse::from);
    }

    /**
     * 맛집 상세 조회
     * - 맛집 기본 정보 + 해당 맛집의 리뷰 목록을 함께 반환
     *
     * @param restaurantId 맛집 ID
     * @return 맛집 상세 정보 (리뷰 포함)
     */
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
}