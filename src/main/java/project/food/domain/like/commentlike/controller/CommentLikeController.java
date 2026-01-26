package project.food.domain.like.commentlike.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.food.domain.like.commentlike.dto.CommentLikeCountDto;
import project.food.domain.like.commentlike.dto.CommentLikeResponseDto;
import project.food.domain.like.commentlike.service.CommentLikeService;

import java.util.List;

@Tag(name = "Comment Like", description = "댓글 좋아요 관리 API")
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
    @Operation(summary = "댓글 좋아요 추가", description = "댓글에 좋아요를 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "좋아요 추가 성공",
                    content = @Content(schema = @Schema(implementation = CommentLikeResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 또는 댓글을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 좋아요를 누른 댓글")
    })
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
    @Operation(summary = "댓글 좋아요 취소", description = "댓글의 좋아요를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좋아요 취소 성공"),
            @ApiResponse(responseCode = "404", description = "좋아요를 찾을 수 없음")
    })
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
    @Operation(summary = "댓글 좋아요 토글", description = "좋아요가 있으면 취소하고, 없으면 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공 (true: 추가됨, false: 취소됨)",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "회원 또는 댓글을 찾을 수 없음")
    })
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
    @Operation(summary = "댓글 좋아요 개수 조회", description = "댓글의 좋아요 개수와 사용자의 좋아요 여부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CommentLikeCountDto.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
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
    @Operation(summary = "회원이 좋아요한 댓글 목록", description = "특정 회원이 좋아요를 누른 댓글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CommentLikeResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
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
    @Operation(summary = "댓글의 좋아요 목록", description = "특정 댓글에 좋아요를 누른 사용자 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = CommentLikeResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @GetMapping("/{commentId}/likes")
    public ResponseEntity<List<CommentLikeResponseDto>> getLikesByComment(
            @PathVariable Long commentId) {
        List<CommentLikeResponseDto> responseDto = commentLikeService.getLikesByComment(commentId);
        return ResponseEntity.ok(responseDto);
    }
}
