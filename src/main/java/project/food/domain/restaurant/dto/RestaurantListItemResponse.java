package project.food.domain.restaurant.dto;

import lombok.Builder;
import lombok.Getter;
import project.food.domain.restaurant.entity.Restaurant;

/**
 * 맛집 목록 응답 DTO
 * - 목록 조회 시 간략 정보만 반환 (좌표, URL 등 제외)
 */
@Getter
@Builder
public class RestaurantListItemResponse {

    private Long id;         // 맛집 ID
    private String name;     // 맛집 이름
    private String address;  // 주소
    private String category; // 카테고리

    public static RestaurantListItemResponse from(Restaurant r) {
        return RestaurantListItemResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .address(r.getAddress())
                .category(r.getCategory())
                .build();
    }
}