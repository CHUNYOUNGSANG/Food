package project.food.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "게시글 수정 요청 DTO")
@Getter
@Setter
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
    @Min(value = 0, message = "평점은 0.0 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5.0 이하여야 합니다.")
    private Double rating;

    @Schema(description = "새로 추가할 이미지 파일 목록")
    @Builder.Default
    private List<MultipartFile> newImages = new ArrayList<>();

    @Schema(description = "삭제할 이미지 ID 목록", example = "[1, 2, 3]")
    @Builder.Default
    private List<Long> deleteImageIds = new ArrayList<>();

    @Schema(description = "수정할 태그 이름 목록 (전체 교체)")
    private List<String> tagNames;

}
