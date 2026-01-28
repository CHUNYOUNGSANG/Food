package project.food.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "게시글 생성 요청 DTO")
@Getter
@Setter
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
    @Min(value = 0, message = "평점은 0.0 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5.0 이하여야 합니다.")
    private Double rating;

    @Schema(description = "업로드할 이미지 파일 목록 (최대 10개)")
    @Builder.Default
    private List<MultipartFile> images = new ArrayList<>();

}
