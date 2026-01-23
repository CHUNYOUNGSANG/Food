package project.food.domain.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "댓글 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    /**
     * 댓글 ID
     */
    @Schema(description = "댓글 ID", example = "1")
    private Long id;

    /**
     * 게시글 ID
     */
    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    /**
     * 작성자 ID
     */
    @Schema(description = "작성자 ID", example = "2")
    private Long memberId;

    /**
     * 작성자 닉네임
     */
    @Schema(description = "작성자 닉네임", example = "맛집러버")
    private String memberNickname;

    /**
     * 댓글 내용
     */
    @Schema(description = "댓글 내용", example = "정말 맛있어 보여요! 저도 가보고 싶어요")
    private String content;

    /**
     * 부모 댓글 ID (대댓글인 경우)
     * - null: 일반 댓글
     * - not null: 대댓글
     */
    @Schema(description = "대댓글 내용", example = "맘에드시나요")
    private Long parentCommentId;

    /**
     * 생성 시간
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
