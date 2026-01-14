package project.food.domain.postlike.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import project.food.domain.postlike.entity.PostLike;

import java.time.LocalDateTime;

/**
 * 좋아요 응답 DTO
 * - 좋아요 정보 전달 시 사용
 */
@Getter
@Builder
public class PostLikeResponseDto {

    private Long id;

    private Long memberId;

    private String nickname;

    private Long postId;

    private String title;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * PostLike Entity를 PostLikeResponseDTO로 변환
     * @param postLike
     * @return
     */
    public static PostLikeResponseDto from(PostLike postLike) {
        return PostLikeResponseDto.builder()
                .id(postLike.getId())
                .memberId(postLike.getMember().getId())
                .nickname(postLike.getMember().getNickname())
                .postId(postLike.getPost().getId())
                .title(postLike.getPost().getTitle())
                .createdAt(postLike.getCreatedAt())
                .build();
    }

    /**
     * 단순 응답용(좋아요 추가/취소 시 사용)
     */
    public static PostLikeResponseDto simple(PostLike postLike) {
        return PostLikeResponseDto.builder()
                .id(postLike.getId())
                .memberId(postLike.getMember().getId())
                .postId(postLike.getPost().getId())
                .createdAt(postLike.getCreatedAt())
                .build();
    }
}
