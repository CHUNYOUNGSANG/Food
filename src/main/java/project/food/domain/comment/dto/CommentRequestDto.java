package project.food.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 작성/수정 요청 DTO
 * - 댓글 작성 시
 * - 댓글 수정 시
 */
@Schema(description = "댓글 생성/수정 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDto {

    /**
     * 댓글 내용
     * - 필수 입력
     *  - 1자 이상 500자 이하
     */
    @Schema(description = "댓글 내용", example = "정말 맛있어 보여요! 저도 가보고 싶어요.")
    @NotBlank(message = "댓글 내용을 입력해주세요")
    @Size(min = 1, max = 500, message = "댓글은 1자 이상 500자 이하로 입력해주세요.")
    private String content;

    /**
     * 부모 댓글 ID (대댓글인 경우에만)
     * - null: 일반 댓글
     * - not null: 대댓글
     */
    @Schema(description = "대댓글 내용", example = "맘에드시나요")
    private Long parentCommentId;
}
