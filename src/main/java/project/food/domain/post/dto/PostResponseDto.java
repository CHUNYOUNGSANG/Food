package project.food.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import project.food.domain.post.entity.Post;
import project.food.domain.post.entity.PostImage;
import project.food.domain.restaurant.entity.Restaurant;

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

    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "작성자 ID", example = "1")
    private Long memberId;

    @Schema(description = "작성자 닉네임", example = "맛집러버")
    private String memberNickname;

    @Schema(description = "게시글 제목", example = "강남 맛집 추천!")
    private String title;

    @Schema(description = "게시글 내용", example = "여기 정말 맛있어요. 강추합니다!")
    private String content;

    @Schema(description = "평점 (0.0 ~ 5.0)", example = "4.5")
    private Double rating;

    @Schema(description = "4.5점", example = "4.5점")
    private String ratingText;

    @Schema(description = "조회수", example = "150")
    private Integer viewCount;

    @Schema(description = "게시글 이미지 목록")
    private List<ImageInfo> images;

    @Schema(description = "태그 목록")
    private List<String> tags;

    @Schema(description = "음식점 정보")
    private RestaurantInfo restaurant;

    @Schema(description = "생성일시", example = "2026-01-22T15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2026-01-22T16:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Post Entity를 PostResponse DTO로 변환
     */
    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .memberId(post.getMember().getId())
                .memberNickname(post.getMember().getNickname())
                .title(post.getTitle())
                .content(post.getContent())
                .rating(post.getRating())
                .ratingText(post.getRatingAsString())
                .viewCount(post.getViewCount())
                .images(post.getImages().stream()
                        .map(ImageInfo::from)
                        .collect(Collectors.toList()))
                .tags(post.getPostTags().stream()
                        .map(postTag -> postTag.getTag().getName())
                        .collect(Collectors.toList()))
                .restaurant(post.getRestaurant() != null
                        ? RestaurantInfo.from(post.getRestaurant())
                        : null)
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
                    .fileUrl(postImage.getFilePath())
                    .fileSize(postImage.getFileSize())
                    .displayOrder(postImage.getDisplayOrder())
                    .build();
        }
    }

    @Schema(description = "음식점 정보")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RestaurantInfo {
        @Schema(description = "음식점 ID", example = "1")
        private Long id;

        @Schema(description = "음식점 이름", example = "서울식당")
        private String name;

        @Schema(description = "음식점 주소", example = "서울시 강남구 역삼동")
        private String address;

        @Schema(description = "음식 카테고리", example = "음식점 > 한식")
        private String category;

        @Schema(description = "위도")
        private Double latitude;

        @Schema(description = "경도")
        private Double longitude;

        @Schema(description = "카카오맵 URL")
        private String placeUrl;

        public static RestaurantInfo from(Restaurant restaurant) {
            return RestaurantInfo.builder()
                    .id(restaurant.getId())
                    .name(restaurant.getName())
                    .address(restaurant.getAddress())
                    .category(restaurant.getCategory())
                    .latitude(restaurant.getLatitude())
                    .longitude(restaurant.getLongitude())
                    .placeUrl(restaurant.getPlaceUrl())
                    .build();
        }
    }
}
