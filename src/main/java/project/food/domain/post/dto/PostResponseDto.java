package project.food.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import project.food.domain.post.entity.Post;
import project.food.domain.post.entity.PostImage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 게시글 응답 DTO
 * - 클라이언트에게 게시글 정보를 전달할 때 사용
 */
@Schema(description = "게시글 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {
    /**
     * 게시글 ID
     */
    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    /**
     * 작성자 ID
     */
    @Schema(description = "작성자 ID", example = "1")
    private Long memberId;

    /**
     * 작성자 닉네임
     */
    @Schema(description = "작성자 닉네임", example = "맛집러버")
    private String memberNickname;

    /**
     * 게시글 제목
     */
    @Schema(description = "게시글 제목", example = "강남 맛집 추천!")
    private String title;

    /**
     * 게시글 내용
     */
    @Schema(description = "게시글 내용", example = "여기 정말 맛있어요. 강추합니다!")
    private String content;

    /**
     * 맛집 이름
     */
    @Schema(description = "음식점 이름", example = "서울식당")
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
    private String foodCategory;

    /**
     * 평점 (0.0 ~ 5.0)
     */
    @Schema(description = "평점 (0.0 ~ 5.0)", example = "4.5")
    private Double rating;

    /**
     * 예: 4.5점
     */
    @Schema(description = "4.5점", example = "4.5점")
    private String ratingText;

    /**
     * 이미지 URL
     */
    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    /**
     * 조회수
     */
    @Schema(description = "조회수", example = "150")
    private Integer viewCount;

    @Schema(description = "게시글 이미지 목록")
    private List<ImageInfo> images;

    /**
     * 작성 시간
     */
    @Schema(description = "생성일시", example = "2026-01-22T15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @Schema(description = "수정일시", example = "2026-01-22T16:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Post Entity를 PostResponse DTO로 변환
     * @param post Post 엔티티
     * @return PostResponse DTO
     */
    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .memberId(post.getMember().getId())
                .memberNickname(post.getMember().getNickname())
                .title(post.getTitle())
                .content(post.getContent())
                .restaurantName(post.getRestaurantName())
                .restaurantAddress(post.getRestaurantAddress())
                .foodCategory(post.getFoodCategory())
                .rating(post.getRating())
                .ratingText(post.getRatingAsString())
                .viewCount(post.getViewCount())
                .images(post.getImages().stream()
                        .map(ImageInfo::from)
                        .collect(Collectors.toList()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();

    }

    @Schema(description = "이미지 정보")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageInfo {
        @Schema(description = "이미지 ID", example = "1")
        private Long id;

        @Schema(description = "원본 파일명", example = "food.jpg")
        private String originalFileName;

        @Schema(description = "이미지 URL", example = "/uploads/post/abc-def.jpg")
        private String fileUrl;

        @Schema(description = "파일 크기 (bytes)", example = "1024000")
        private Long fileSize;

        @Schema(description = "표시 순서", example = "0")
        private Integer displayOrder;

        public static ImageInfo from(PostImage postImage) {
            return ImageInfo.builder()
                    .id(postImage.getId())
                    .originalFileName(postImage.getOriginalFileName())
                    .fileUrl("/uploads/post/" + postImage.getStoredFileName())
                    .fileSize(postImage.getFileSize())
                    .displayOrder(postImage.getDisplayOrder())
                    .build();
        }
    }
}
