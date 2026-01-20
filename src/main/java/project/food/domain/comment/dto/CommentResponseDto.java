package project.food.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.food.domain.comment.entity.Comment;

import java.time.LocalDateTime;

/**
 * 댓글 응답 DTO
 * - 댓글 조회 시
 * - 댓글 작성/수정 후 응답
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    /**
     * 댓글 ID
     */
    private Long id;

    /**
     * 게시글 ID
     */
    private Long postId;

    /**
     * 작성자 ID
     */
    private Long memberId;

    /**
     * 작성자 닉네임
     */
    private String memberNickname;

    /**
     * 댓글 내용
     */
    private String content;

    /**
     * 부모 댓글 ID (대댓글인 경우)
     * - null: 일반 댓글
     * - not null: 대댓글
     */
    private Long parentCommentId;

    /**
     * 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    private LocalDateTime updatedAt;

    /**
     * Entity -> DTO 변환 (정적 팩토리 메서드)
     * @param comment 댓글 엔티티
     * @return CommentResponseDto
     */
    public static CommentResponseDto from(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .memberId(comment.getMemberId())
                .memberNickname(comment.getMemberNickname())
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();

    }
}
