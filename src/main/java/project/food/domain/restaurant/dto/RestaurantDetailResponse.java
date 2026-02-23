package project.food.domain.restaurant.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import project.food.domain.restaurant.entity.Restaurant;

/**
 * 맛집 상세 응답 DTO
 * - 맛집 기본 정보 + 리뷰 목록을 함께 반환
 */
@Getter
@Builder
public class RestaurantDetailResponse {

    private Long id;           // 맛집 ID
    private String name;       // 맛집 이름
    private String address;    // 주소
    private String category;   // 카테고리 (한식, 일식 등)
    private Double latitude;   // 위도
    private Double longitude;  // 경도
    private String placeUrl;   // 카카오 플레이스 URL

    private Page<RestaurantReviewItemResponse> reviews; // 리뷰 목록 (페이지네이션)

    /**
     * Restaurant 엔티티 + 리뷰 목록으로 상세 응답 생성
     */
    public static RestaurantDetailResponse of(Restaurant r, Page<RestaurantReviewItemResponse> reviews) {
        return RestaurantDetailResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .address(r.getAddress())
                .category(r.getCategory())
                .latitude(r.getLatitude())
                .longitude(r.getLongitude())
                .placeUrl(r.getPlaceUrl())
                .reviews(reviews)
                .build();
    }
}