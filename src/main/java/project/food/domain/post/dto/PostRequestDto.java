package project.food.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "게시글 생성 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequestDto {
    /**
     * 게시글 제목
     */
    @Schema(description = "게시글 제목", example = "강남 맛집 추천!")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
    private String title;

    /**
     * 게시글 내용
     */
    @Schema(description = "게시글 내용", example = "여기 진짜 맛있어요. 강추합니다!")
    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    /**
     * 맛집 이름
     */
    @Schema(description = "음식점 이름", defaultValue = "서울식당")
    private String restaurantName;

    /**
     * 맛집 주소
     */
    @Schema(description = "음식점 주소", example = "서울시 강남구 역삼동")
    private String restaurantAddress;

    /**
     * 음식 카테고리
     */
    @Schema(description = "음식 카테고리", example = "한식")
    @NotBlank(message = "카테고리는 필수입니다.")
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
