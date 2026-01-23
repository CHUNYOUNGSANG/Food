package project.food.domain.like.commentlike.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.food.domain.like.commentlike.dto.CommentLikeCountDto;
import project.food.domain.like.commentlike.dto.CommentLikeResponseDto;
import project.food.domain.like.commentlike.service.CommentLikeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    /**
     * 댓글 좋아요 추가
     * @param memberId
     * @param commentId
     * @return 생성된 좋아요 정보
     * 성공: 201 Created, 실패(중복): 409 Conflict, 실패(댓글 없음): 404 Not Found
     */
    @PostMapping("/{commentId}/likes")
    public ResponseEntity<CommentLikeResponseDto> addLike(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long commentId) {
        CommentLikeResponseDto responseDto = commentLikeService.addLike(memberId, commentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 댓글 좋아요 취소
     * @param memberId
     * @param commentId
     * @return 성공: 204 No Content, 실패(좋아요 없음): 404 Not Found
     */
    @DeleteMapping("/{commentId}/likes")
    public ResponseEntity<Void> removeLike(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long commentId) {
        commentLikeService.removeLike(memberId, commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 댓글 좋아요 토글 (있으면 취소, 없으면 추가)
     * @param memberId
     * @param commentId
     * @return 200 OK + 좋아요 추가 여부 (true: 추가, false: 취소)
     */
    @PutMapping("/{commentId}/likes/toggle")
    public ResponseEntity<Boolean> likeToggle(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long commentId) {
        boolean isLiked = commentLikeService.likeToggle(memberId, commentId);
        return ResponseEntity.ok(isLiked);
    }

    /**
     * 댓글의 졸아요 개수 및 사용자의 좋아요 여부 조회
     * @param memberId
     * @param commentId
     * @return 200 OK + 좋아요 개수 및 사용자의 좋아요 여부
     */
    @GetMapping("/{commentId}/likes/count")
    public ResponseEntity<CommentLikeCountDto> getLikeCount(
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @PathVariable Long commentId) {
        CommentLikeCountDto likeCountDto = commentLikeService.getLikeCount(memberId, commentId);
        return ResponseEntity.ok(likeCountDto);
    }

    /**
     * 특정 회원이 좋아요한 댓글 목록 조회
     * @param memberId
     * @return 200 OK + 좋아요한 댓글 목록 (최신순)
     */
    @GetMapping("/likes/member/{memberId}")
    public ResponseEntity<List<CommentLikeResponseDto>> getLikedCommentsByMember(
            @PathVariable Long memberId) {
        List<CommentLikeResponseDto> responseDto = commentLikeService.getLikedCommentsByMember(memberId);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 특정 댓글의 좋아요 목록 조회 (누가 좋아요 눌렀는지)
     * @param commentId
     * @return 200 OK + 좋아요 목록 (최신순, 회원 정보 포함)
     */
    @GetMapping("/{commentId}/likes")
    public ResponseEntity<List<CommentLikeResponseDto>> getLikesByComment(
            @PathVariable Long commentId) {
        List<CommentLikeResponseDto> responseDto = commentLikeService.getLikesByComment(commentId);
        return ResponseEntity.ok(responseDto);
    }
}
