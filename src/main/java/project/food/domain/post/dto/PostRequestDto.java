package project.food.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequestDto {
    /**
     * 게시글 제목
     */
    private String title;

    /**
     * 게시글 내용
     */
    private String content;

    /**
     * 맛집 이름
     */
    private String restaurantName;

    /**
     * 맛집 주소
     */
    private String restaurantAddress;

    /**
     * 음식 카테고리
     */
    private String foodCategory;

    /**
     * 평점 (0.0 ~ 5.0)
     */
    private BigDecimal rating;

    /**
     * 이미지 URL
     */
    private String imageUrl;

}
