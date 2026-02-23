package project.food.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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
     * 연결할 음식점 ID (이미 DB에 있는 맛집)
     */
    @Schema(description = "음식점 ID (DB에 있는 맛집)", example = "1")
    private Long restaurantId;

    // ── 카카오 맛집 정보 (새로 등록할 때) ──

    @Schema(description = "카카오 place ID", example = "12345678")
    private String placeId;

    @Schema(description = "맛집 이름", example = "서울식당")
    private String placeName;

    @Schema(description = "맛집 주소", example = "서울시 강남구 역삼동 123")
    private String placeAddress;

    @Schema(description = "카테고리", example = "음식점 > 한식")
    private String placeCategory;

    @Schema(description = "위도", example = "37.1234")
    private Double placeLatitude;

    @Schema(description = "경도", example = "127.1234")
    private Double placeLongitude;

    @Schema(description = "카카오맵 URL", example = "https://place.map.kakao.com/12345678")
    private String placeUrl;

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

    @Schema(description = "태그 이름 목록")
    @Builder.Default
    private List<String> tagNames = new ArrayList<>();

}
