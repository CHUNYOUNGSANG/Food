package project.food.domain.like.commentlike.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentLikeCountDto {

    private Long commentId;

    /**
     * 좋아요 개수
     */
    private Long likeCount;

    /**
     * 사용자의 좋아요 여부
     * - true: 좋아요 누른 상태
     * - false: 좋아요 안 누른 상태
     * - null: 비로그인 사용자 (false)
     */
    private Boolean isLiked;
}
