package project.food.domain.post.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@Tag(name = "Post", description = "게시글 API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    /**
     * 게시글 생성
     * POST /api/posts
     * @param memberId 작성자 ID (헤더)
     * @param request 게시글 생성 요청 데이터
     * @return 생성된 게시글 정보 (201 Created)
     */
    @Operation(summary = "게시글 생성", description = "새로운 게시글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDto> createPost(
            @Parameter(description = "작성자 회원 ID", required = true)
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "게시글 생성 요청 데이터", required = true)
            @ModelAttribute PostRequestDto request) {

        log.info("게시글 생성 요청: memberId={}, title={}, restaurantName={}, category={}, rating={}, imageCount={}",
                memberId,
                request.getTitle(),
                request.getRestaurantName(),
                request.getFoodCategory(),
                request.getRating(),
                request.getImages() != null ? request.getImages().size() : 0);

        PostResponseDto response = postService.createPost(memberId, request);

        log.info("✅ 게시글 생성 완료: postId={}, memberId={}", response.getId(), memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 게시글 전체 목록 조회 (최신순)
     * GET /api/posts
     * @return 전체 게시글 목록 (200 OK)
     */
    @Operation(summary = "게시글 전체 목록 조회 (최신순)", description = "모든 게시글을 최신순으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PostResponseDto.class)))
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {

        log.info("게시글 전체 목록 조회 요청");

        List<PostResponseDto> posts = postService.getAllPosts();

        log.info("✅ 게시글 전체 목록 조회 완료: postCount={}", posts.size());

        return ResponseEntity.ok(posts);
    }

    /**
     * 게시글 상세 조회
     * GET /api/posts/{postId}
     * @param postId 게시글 ID
     * @return 게시글 상세 정보 (200 OK)
     */
    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상제 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId) {

        log.info("게시글 상세 조회 요청: postId={}", postId);

        PostResponseDto response = postService.getPost(postId);

        log.info("✅ 게시글 상세 조회 완료: postId={}, title={}, viewCount={}",
                postId, response.getTitle(), response.getViewCount());

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
    @Operation(summary = "게시글 수정", description = "작성한 게시글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDto> updatePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @Parameter(description = "수정 요청자 회원 ID", required = true)
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "게시글 수정 데이터 (multipart/form-data)", required = true)
            @ModelAttribute PostUpdateDto request) {

        log.info("게시글 수정 요청: postId={}, memberId={}, title={}, newImageCount={}, deleteImageCount={}",
                postId,
                memberId,
                request.getTitle(),
                request.getNewImages() != null ? request.getNewImages().size() : 0,
                request.getDeleteImageIds() != null ? request.getDeleteImageIds().size() : 0);

        PostResponseDto response = postService.updatePost(postId, memberId, request);

        log.info("✅ 게시글 수정 완료: postId={}, memberId={}", postId, memberId);

        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 삭제
     * DELETE /api/posts/{postId}
     * @param postId 게시글 ID
     * @param memberId 삭제 요청자 ID (헤더)
     * @return 204 No Content
     */
    @Operation(summary = "게시글 삭제", description = "작성한 게시글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @Parameter(description = "삭제 요청자 회원 ID", required = true)
            @AuthenticationPrincipal Long memberId) {

        log.info("게시글 삭제 요청: postId={}, memberId={}", postId, memberId);

        postService.deletePost(postId, memberId);

        log.info("✅ 게시글 삭제 완료: postId={}, memberId={}", postId, memberId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 회원의 게시글 목록 조회
     * GET /api/posts/member/{memberId}
     * @param memberId 회원 ID
     * @return 해당 회원의 게시글 목록 (200 OK)
     */
    @Operation(summary = "회원별 게시글 조회", description = "특정 회원이 작성한 모든 게시글을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PostResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<PostResponseDto>> getPostsByMember(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable Long memberId) {

        log.info("회원별 게시글 조회 요청: memberId={}", memberId);

        List<PostResponseDto> posts = postService.getPostsByMember(memberId);

        log.info("✅ 회원별 게시글 조회 완료: memberId={}, postCount={}",
                memberId, posts.size());

        return ResponseEntity.ok(posts);
    }

    /**
     * 음식 카테고리별 게시글 조회
     * GET /api/posts/category/{foodCategory}
     * @param foodCategory 음식 카테고리
     * @return 해당 카테고리의 게시글 목록 (200 OK)
     */
    @Operation(summary = "카테고리별 게시글 조회", description = "특정 음식 카테고리의 게시글을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PostResponseDto.class)))
    @GetMapping("/category/{foodCategory}")
    public ResponseEntity<List<PostResponseDto>> getPostsByCategory(
            @Parameter(description = "음식 카테고리 (예: 한식, 중식, 양식)", required = true)
            @PathVariable String foodCategory) {

        log.info("카테고리별 게시글 조회 요청: category={}", foodCategory);

        List<PostResponseDto> posts = postService.getPostsByCategory(foodCategory);

        log.info("✅ 카테고리별 게시글 조회 완료: category={}, postCount={}",
                foodCategory, posts.size());

        return ResponseEntity.ok(posts);
    }


    /**
     * 게시글 검색
     * GET /api/posts/search?keyword=맛집
     * @param keyword 검색 키워드
     * @return 검색 결과 게시글 목록 (200 OK)
     */
    @Operation(summary = "게시글 검색", description = "제목 또는 내용에 키워드가 포함된 게시글을 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색 성공",
            content = @Content(schema = @Schema(implementation = PostResponseDto.class)))
    @GetMapping("/search")
    public ResponseEntity<List<PostResponseDto>> searchPosts(
            @Parameter(description = "검색 키워드", required = true, example = "맛집")
            @RequestParam String keyword) {

        log.info("게시글 검색 요청: keyword={}", keyword);

        List<PostResponseDto> posts = postService.searchPosts(keyword);

        log.info("✅ 게시글 검색 완료: keyword={}, resultCount={}",
                keyword, posts.size());

        return ResponseEntity.ok(posts);
    }
}