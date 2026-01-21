package project.food.domain.postlike.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.food.domain.postlike.dto.PostLikeCountDTO;
import project.food.domain.postlike.dto.PostLikeResponseDto;
import project.food.domain.postlike.service.PostLikeService;

import java.util.List;

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
    @GetMapping("/{postId}/likes")
    public ResponseEntity<List<PostLikeResponseDto>> getLikesByPost(@PathVariable Long postId) {
        List<PostLikeResponseDto> responseDto = postLikeService.getLikesByPost(postId);
        return ResponseEntity.ok(responseDto);
    }
}
