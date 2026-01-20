package project.food.domain.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.food.domain.comment.dto.CommentRequestDto;
import project.food.domain.comment.dto.CommentResponseDto;
import project.food.domain.comment.service.CommentService;

import java.util.List;

/**
 * Comment Controller
 *
 * API 목록:
 * - Post       /api/posts/{postId}/comments        : 댓글 작성
 * - GET        /api/posts/{postId}/comments        : 게시글의 댓글 목록 조회
 * - PUT        /api/posts/{postId}/comments/{id}   : 댓글 수정
 * - DELETE     /api/posts/{postId}/comments/{id}   : 댓글 삭제
 * - GET        /api/members/{memberId}/comments    : 회원의 댓글 목록 조회
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     *
     * POST /api/posts/{postId}/comments
     *
     * @param postId
     * @param memberId
     * @param requestDto
     * @return
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody CommentRequestDto requestDto) {

        CommentResponseDto response = commentService.createComment(postId, memberId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 게시글의 댓글 목록 조회
     *
     * GET /api/posts/{postId}/comments
     * @param postId 게시글 ID
     * @return 200 OK + 댓글 목록 (오래된 순)
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentResponseDto> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 특정 회원의 댓글 목록 조회
     *
     * GET /api/members/{memberId}/comments
     *
     * @param memberId 회원 ID
     * @return 200 OK + 댓글 목록 (최신순)
     */
    @GetMapping("/members/{memberId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByMember(@PathVariable Long memberId) {
        List<CommentResponseDto> comments = commentService.getCommentsByMemberId(memberId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 수정
     *
     * PUT /api/posts/{postId}/comments/{commentId}
     *
     * @param postId 게시글 ID
     * @param commentId 댓글 ID
     * @param memberId 수정 요청자 ID
     * @param requestDto 수정 내용
     * @return 200 OK + 수정된 댓글 정보
     */
    @PutMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody CommentRequestDto requestDto) {

        CommentResponseDto response = commentService.updateComment(commentId, memberId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 삭제
     *
     * DELETE /api/posts/{postId}/comments/{commentId}
     * @param postId
     * @param commentId
     * @param memberId
     * @return
     */
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestHeader("X-Member-Id") Long memberId) {

        commentService.deleteComment(commentId, memberId);
        return ResponseEntity.noContent().build();
    }



}
