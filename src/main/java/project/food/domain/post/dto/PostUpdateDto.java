package project.food.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "게시글 수정 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateDto {
    /**
     * 게시글 제목
     */
    @Schema(description = "게시글 제목", example = "수정된 제목")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
    private String title;

    /**
     * 게시글 내용
     */
    @Schema(description = "게시글 내용", example = "수정된 내용입니다.")
    private String content;

    /**
     * 맛집 이름
     */
    @Schema(description = "음식점 이름", example = "베이징식당")
    private String restaurantName;

    /**
     * 맛집 주소
     */
    @Schema(description = "음식점 주소", example = "서울시 강남구 청담동")
    private String restaurantAddress;

    /**
     * 음식 카테고리
     */
    @Schema(description = "음식 카테고리", example = "중식")
    private String foodCategory;

    /**
     * 평점 (0.0 ~ 5.0)
     */
    @Schema(description = "평점 (0.0 ~ 5.0)", example = "4.5")
    private BigDecimal rating;

    /**
     * 이미지 URL
     */
    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

}
