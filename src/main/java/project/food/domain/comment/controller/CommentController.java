package project.food.domain.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 작성 성공",
                    content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
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
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @Parameter(description = "작성자 회원 ID", required = true)
            @RequestHeader("Member-Id") Long memberId,
            @Parameter(description = "댓글 내용", required = true)
            @Valid @RequestBody CommentRequestDto requestDto) {

        CommentResponseDto response = commentService.createComment(postId, memberId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "게시글의 댓글 목록 조회 (오래된 순)", description = "특정 게시글의 모든 댓글을 오래된 순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    /**
     * 특정 게시글의 댓글 목록 조회
     *
     * GET /api/posts/{postId}/comments
     * @param postId 게시글 ID
     * @return 200 OK + 댓글 목록 (오래된 순)
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId) {
        List<CommentResponseDto> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "회원의 댓글 목록 조회", description = "특정 회원이 작성한 모든 댓글을 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    /**
     * 특정 회원의 댓글 목록 조회
     *
     * GET /api/members/{memberId}/comments
     *
     * @param memberId 회원 ID
     * @return 200 OK + 댓글 목록 (최신순)
     */
    @GetMapping("/members/{memberId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByMember(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Long memberId) {
        List<CommentResponseDto> comments = commentService.getCommentsByMemberId(memberId);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "댓글 수정", description = "작성한 댓글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
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
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "수정 요청자 회원 ID", required = true)
            @RequestHeader("Member-Id") Long memberId,
            @Parameter(description = "수정할 댓글 내용", required = true)
            @Valid @RequestBody CommentRequestDto requestDto) {

        CommentResponseDto response = commentService.updateComment(commentId, memberId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 삭제", description = "작성한 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
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
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "삭제 요청자 회원 ID", required = true)
            @RequestHeader("Member-Id") Long memberId) {

        commentService.deleteComment(commentId, memberId);
        return ResponseEntity.noContent().build();
    }



}
