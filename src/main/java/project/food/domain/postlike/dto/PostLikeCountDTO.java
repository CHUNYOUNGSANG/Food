package project.food.domain.postlike.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 게시글 좋아요 수 응답 DTO
 * - 게시글의 좋아요 개수와 사용자의 좋아요 여부 전달
 */
@Getter
@Builder
public class PostLikeCountDTO {

    private Long postId;

    private Long likeCount;

    private Boolean isLiked;
}
