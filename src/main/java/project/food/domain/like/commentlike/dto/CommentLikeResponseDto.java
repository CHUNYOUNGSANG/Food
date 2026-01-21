package project.food.domain.like.commentlike.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import project.food.domain.like.commentlike.entity.CommentLike;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentLikeResponseDto {

    private Long id;

    private Long memberId;

    /**
     * 좋아요를 누른 회원 닉네임
     */
    private String nickname;

    private Long commentId;

    /**
     * 댓글 내용 (미리보기용)
     */
    private String content;

    private Long postId;

    /**
     * 게시글 제목 (미리보기용)
     */
    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * @param commentLike
     * @return CommentLikeResponseDTO (전체 정보)
     */
    public static CommentLikeResponseDto from(CommentLike commentLike) {
        return CommentLikeResponseDto.builder()
                .id(commentLike.getId())
                .memberId(commentLike.getMember().getId())
                .nickname(commentLike.getMember().getNickname())
                .commentId(commentLike.getComment().getId())
                .content(commentLike.getComment().getContent())
                .postId(commentLike.getComment().getPost().getId())
                .title(commentLike.getComment().getPost().getTitle())
                .createdAt(commentLike.getCreatedAt())
                .build();
    }

    /**
     * @param commentLike
     * @return CommentLikeResponseDTO (간단한 정보)
     */
    public static CommentLikeResponseDto simple(CommentLike commentLike) {
        return CommentLikeResponseDto.builder()
                .id(commentLike.getId())
                .memberId(commentLike.getMember().getId())
                .commentId(commentLike.getComment().getId())
                .createdAt(commentLike.getCreatedAt())
                .build();
    }
}
