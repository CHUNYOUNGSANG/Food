package project.food.domain.restaurant.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import project.food.domain.restaurant.dto.RestaurantDetailResponse;
import project.food.domain.restaurant.dto.RestaurantListItemResponse;
import project.food.domain.restaurant.dto.RestaurantRankResponse;
import project.food.domain.restaurant.dto.RestaurantReviewItemResponse;
import project.food.domain.restaurant.service.RestaurantService;
import project.food.global.common.CursorPageResponse;

/**
 * 맛집 조회 API 컨트롤러
 * - GET /api/restaurants       : 목록 조회 (통합 검색)
 * - GET /api/restaurants/{id}  : 상세 조회 (리뷰 포함)
 * - GET /api/restaurants/{id}/posts : 맛집별 리뷰 목록 조회
 */
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    /**
     * 맛집 목록 조회 / 통합 검색
     * - q 파라미터 없으면 전체 목록
     * - q 파라미터 있으면 이름/주소/카테고리 검색
     *
     * ex) GET /api/restaurants?q=강남&page=0&size=10
     */
    @GetMapping
    public Page<RestaurantListItemResponse> search(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return restaurantService.search(q, page, size);
    }

    /**
     * 맛집 상세 조회 (리뷰 포함)
     *
     * ex) GET /api/restaurants/5
     */
    @GetMapping("/{restaurantId}")
    public RestaurantDetailResponse detail(
            @PathVariable Long restaurantId
    ) {
        return restaurantService.getDetail(restaurantId);
    }

    /**
     * 맛집별 리뷰(게시글) 목록 조회
     *
     * ex) GET /api/restaurants/5/posts?page=0&size=10
     */
    @GetMapping("/{restaurantId}/posts")
    public Page<RestaurantReviewItemResponse> posts(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return restaurantService.getRestaurantPosts(restaurantId, page, size);
    }

    /**
     * 추천맛집 목록 (무한 스크롤, 평균 평점 높은 순)
     *
     * 첫 요청: GET /api/restaurants/recommended?size=10
     * 이후:    GET /api/restaurants/recommended?cursor=XXX&size=10
     */
    @GetMapping("/recommended")
    public CursorPageResponse<RestaurantRankResponse> recommended(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return restaurantService.getRecommended(cursor, size);
    }

    /**
     * 인기맛집 목록 (무한 스크롤, 리뷰 수 많은 순)
     *
     * 첫 요청: GET /api/restaurants/popular?size=10
     * 이후:    GET /api/restaurants/popular?cursor=XXX&size=10
     */
    @GetMapping("/popular")
    public CursorPageResponse<RestaurantRankResponse> popular(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return restaurantService.getPopular(cursor, size);
    }
}