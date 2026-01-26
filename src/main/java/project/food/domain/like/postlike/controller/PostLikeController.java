package project.food.domain.like.postlike.controller;

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
import project.food.domain.like.postlike.dto.PostLikeCountDTO;
import project.food.domain.like.postlike.dto.PostLikeResponseDto;
import project.food.domain.like.postlike.service.PostLikeService;

import java.util.List;

@Tag(name = "Post Like", description = "게시글 좋아요 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostLikeController {

    private final PostLikeService postLikeService;

    /**
     * 좋아요 추가
     * @param memberId
     * @param postId
     * @return 생성된 좋아요 정보
     */
    @Operation(summary = "게시글 좋아요 추가", description = "게시글에 좋아요를 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "좋아요 추가 성공",
                    content = @Content(schema = @Schema(implementation = PostLikeResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "회원 또는 게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 좋아요를 누른 게시글")
    })
    @PostMapping("/{postId}/likes")
    public ResponseEntity<PostLikeResponseDto> addLike(@RequestHeader("Member-Id") Long memberId,
                                                       @PathVariable Long postId) {
        PostLikeResponseDto responseDto = postLikeService.addLike(memberId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    /**
     * 좋아요 취소
     * @param memberId
     * @param postId
     * @return 204 No Content
     */
    @Operation(summary = "게시글 좋아요 취소", description = "게시글의 좋아요를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "좋아요 취소 성공"),
            @ApiResponse(responseCode = "404", description = "좋아요를 찾을 수 없음")
    })
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<Void> removeLike(@RequestHeader("Member-Id") Long memberId,
                                           @PathVariable Long postId) {
        postLikeService.removeLike(memberId, postId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 좋아요 토글 (있으면 취소, 없으면 추가)
     * @param memberId
     * @param postId
     * @return 좋아요 상태 (true: 추가, false: 취소)
     */
    @Operation(summary = "게시글 좋아요 토글", description = "좋아요가 있으면 취소하고, 없으면 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공 (true: 추가됨, false: 취소됨)",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "404", description = "회원 또는 게시글을 찾을 수 없음")
    })
    @PutMapping("/{postId}/likes/toggle")
    public ResponseEntity<Boolean> toggleLike(@RequestHeader("Member-Id") Long memberId,
                                              @PathVariable Long postId) {
        boolean isLikes = postLikeService.toggleLike(memberId, postId);
        return ResponseEntity.ok(isLikes);
    }

    /**
     * 게시글의 좋아요 개수 및 사용자의 좋아요 여부 확인
     * @param memberId
     * @param postId
     * @return 좋아요 개수 및 좋아요 여부
     */
    @Operation(summary = "게시글 좋아요 개수 조회", description = "게시글의 좋아요 개수와 사용자의 좋아요 여부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostLikeCountDTO.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<PostLikeCountDTO> getLikeCount(
            @RequestHeader(value = "Member-Id", required = false) Long memberId,
            @PathVariable Long postId) {
        PostLikeCountDTO likeCountDTO = postLikeService.getLikeCount(memberId, postId);
        return ResponseEntity.ok(likeCountDTO);
    }

    /**
     * 특정 회원이 좋아요한 게시글 목록 조회
     * @param memberId
     * @return 좋아요한 게시글 목록
     */
    @Operation(summary = "회원이 좋아요한 게시글 목록", description = "특정 회원이 좋아요를 누른 게시글 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostLikeResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/likes/member/{memberId}")
    public ResponseEntity<List<PostLikeResponseDto>> getLikedPostsByMember(
            @PathVariable Long memberId) {
        List<PostLikeResponseDto> responseDto = postLikeService.getLikedPostsByMember(memberId);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * 특정 게시글의 좋아요 목록 조회 (누가 좋아요 눌렀는지)
     * @param postId
     * @return 좋아요 목록 (회원 정보 포함)
     */
    @Operation(summary = "게시글의 좋아요 목록", description = "특정 게시글에 좋아요를 누른 사용자 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostLikeResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/{postId}/likes")
    public ResponseEntity<List<PostLikeResponseDto>> getLikesByPost(@PathVariable Long postId) {
        List<PostLikeResponseDto> responseDto = postLikeService.getLikesByPost(postId);
        return ResponseEntity.ok(responseDto);
    }
}
