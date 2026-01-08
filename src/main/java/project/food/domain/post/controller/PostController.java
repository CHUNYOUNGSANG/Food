package project.food.domain.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.food.domain.post.dto.PostRequestDto;
import project.food.domain.post.dto.PostResponseDto;
import project.food.domain.post.dto.PostUpdateDto;
import project.food.domain.post.service.PostService;

import java.util.List;

/**
 * Post Controller
 * 게시글 관련 HTTP 요청 처리
 * RESTful API 구현
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시글 생성
     * POST /api/posts
     * @param memberId 작성자 ID (헤더)
     * @param request 게시글 생성 요청 데이터
     * @return 생성된 게시글 정보 (201 Created)
     */
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @RequestHeader("Member-Id") Long memberId,
            @RequestBody PostRequestDto request) {

        PostResponseDto response = postService.createPost(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 게시글 전체 목록 조회 (최신순)
     * GET /api/posts
     * @return 전체 게시글 목록 (200 OK)
     */
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        List<PostResponseDto> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 상세 조회
     * GET /api/posts/{postId}
     * @param postId 게시글 ID
     * @return 게시글 상세 정보 (200 OK)
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId) {
        PostResponseDto response = postService.getPost(postId);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 수정
     * PUT /api/posts/{postId}
     * @param postId 게시글 ID
     * @param memberId 수정 요청자 ID (헤더)
     * @param request 수정 요청 데이터
     * @return 수정된 게시글 정보 (200 OK)
     */
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestHeader("Member-Id") Long memberId,
            @RequestBody PostUpdateDto request) {

        PostResponseDto response = postService.updatePost(postId, memberId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 삭제
     * DELETE /api/posts/{postId}
     * @param postId 게시글 ID
     * @param memberId 삭제 요청자 ID (헤더)
     * @return 204 No Content
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestHeader("Member-Id") Long memberId) {

        postService.deletePost(postId, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 회원의 게시글 목록 조회
     * GET /api/posts/member/{memberId}
     * @param memberId 회원 ID
     * @return 해당 회원의 게시글 목록 (200 OK)
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PostResponseDto>> getPostsByMember(@PathVariable Long memberId) {
        List<PostResponseDto> posts = postService.getPostsByMember(memberId);
        return ResponseEntity.ok(posts);
    }

    /**
     * 음식 카테고리별 게시글 조회
     * GET /api/posts/category/{foodCategory}
     * @param foodCategory 음식 카테고리
     * @return 해당 카테고리의 게시글 목록 (200 OK)
     */
    @GetMapping("/category/{foodCategory}")
    public ResponseEntity<List<PostResponseDto>> getPostsByCategory(@PathVariable String foodCategory) {
        List<PostResponseDto> posts = postService.getPostsByCategory(foodCategory);
        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 검색
     * GET /api/posts/search?keyword=맛집
     * @param keyword 검색 키워드
     * @return 검색 결과 게시글 목록 (200 OK)
     */
    @GetMapping("/search")
    public ResponseEntity<List<PostResponseDto>> searchPosts(@RequestParam String keyword) {
        List<PostResponseDto> posts = postService.searchPosts(keyword);
        return ResponseEntity.ok(posts);
    }
}