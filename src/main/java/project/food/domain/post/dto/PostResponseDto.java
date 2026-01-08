package project.food.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.food.domain.post.entity.Post;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 게시글 응답 DTO
 * - 클라이언트에게 게시글 정보를 전달할 때 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {
    /**
     * 게시글 ID
     */
    private Long id;

    /**
     * 작성자 ID
     */
    private Long memberId;

    /**
     * 작성자 닉네임
     */
    private String memberNickname;

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
     * 예: 4.5점
     */
    private String ratingText;

    /**
     * 이미지 URL
     */
    private String imageUrl;

    /**
     * 조회수
     */
    private Integer viewCount;

    /**
     * 작성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
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
                .imageUrl(post.getImageUrl())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();

    }
}
