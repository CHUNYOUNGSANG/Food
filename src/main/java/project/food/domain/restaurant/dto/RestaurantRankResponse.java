package project.food.domain.restaurant.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RestaurantRankResponse {
    private Long id;
    private String name;
    private String address;
    private String category;
    private Double avgRating;  // 추천맛집용 (평균 평점)
    private Long postCount;    // 인기맛집용 (리뷰 수)
}
