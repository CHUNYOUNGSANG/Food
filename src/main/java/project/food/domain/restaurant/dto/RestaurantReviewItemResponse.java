package project.food.domain.restaurant.dto;

import lombok.Builder;
import lombok.Getter;
import project.food.domain.post.entity.Post;

import java.time.LocalDateTime;

/**
 * 맛집 리뷰 항목 응답 DTO
 * - 맛집 상세 페이지에서 리뷰 목록을 표시할 때 사용
 */
@Getter
@Builder
public class RestaurantReviewItemResponse {

    private Long postId;            // 게시글 ID
    private String title;           // 리뷰 제목
    private Double rating;          // 평점
    private Long memberId;          // 작성자 ID
    private String memberName;      // 작성자 닉네임
    private Integer viewCount;      // 조회수
    private LocalDateTime createdAt; // 작성일시

    /**
     * Post 엔티티 → 리뷰 응답 DTO 변환
     */
    public static RestaurantReviewItemResponse from(Post p) {
        return RestaurantReviewItemResponse.builder()
                .postId(p.getId())
                .title(p.getTitle())
                .rating(p.getRating())
                .memberId(p.getMember().getId())
                .memberName(p.getMember().getNickname())
                .viewCount(p.getViewCount())
                .createdAt(p.getCreatedAt())
                .build();
    }
}