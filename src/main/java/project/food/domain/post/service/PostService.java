package project.food.domain.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.member.entity.Member;
import project.food.domain.member.repository.MemberRepository;
import project.food.domain.post.dto.PostRequestDto;
import project.food.domain.post.dto.PostResponseDto;
import project.food.domain.post.dto.PostUpdateDto;
import project.food.domain.post.entity.Post;
import project.food.domain.post.repository.PostRepository;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Post Service
 * Global Exception 구조 사용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    /**
     * 게시글 생성
     * @param memberId 작성자 ID
     * @param request 게시글 생성 요청 데이터
     * @return 생성된 게시글 정보
     */
    @Transactional
    public PostResponseDto createPost(Long memberId, PostRequestDto request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Post post = Post.builder()
                .member(member)
                .title(request.getTitle())
                .content(request.getContent())
                .restaurantName(request.getRestaurantName())
                .restaurantAddress(request.getRestaurantAddress())
                .foodCategory(request.getFoodCategory())
                .rating(request.getRating())
                .imageUrl(request.getImageUrl())
                .build();

        Post savedPost = postRepository.save(post);

        return PostResponseDto.from(savedPost);
    }

    /**
     * 게시글 전체 목록 조회 (최신순)
     * @return 게시글 목록
     */
    public List<PostResponseDto> getAllPosts() {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 상세 조회
     * 조회수 증가
     * @param postId 게시글 ID
     * @return 게시글 상세 정보
     */
    @Transactional
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        post.increaseViewCount();

        return PostResponseDto.from(post);
    }

    /**
     * 게시글 수정
     * @param postId 게시글 ID
     * @param memberId 수정 요청자 ID
     * @param request 수정 요청 데이터
     * @return 수정된 게시글 정보
     */
    @Transactional
    public PostResponseDto updatePost(Long postId, Long memberId, PostUpdateDto request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.isWriter(memberId)) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }

        post.updatePost(
                request.getTitle(),
                request.getContent(),
                request.getRestaurantName(),
                request.getRestaurantAddress(),
                request.getFoodCategory(),
                request.getRating(),
                request.getImageUrl()
        );

        return PostResponseDto.from(post);
    }

    /**
     * 게시글 삭제
     * @param postId 게시글 ID
     * @param memberId 삭제 요청자 ID
     */
    @Transactional
    public void deletePost(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.isWriter(memberId)) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }

        postRepository.delete(post);
    }

    /**
     * 특정 회원의 게시글 목록 조회
     * @param memberId 회원 ID
     * @return 해당 회원의 게시글 목록
     */
    public List<PostResponseDto> getPostsByMember(Long memberId) {
        List<Post> posts = postRepository.findByMemberId(memberId);
        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 음식 카테고리별 게시글 조회
     * @param foodCategory 음식 카테고리
     * @return 해당 카테고리의 게시글 목록
     */
    public List<PostResponseDto> getPostsByCategory(String foodCategory) {
        List<Post> posts = postRepository.findByFoodCategory(foodCategory);
        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 검색 (제목)
     * @param keyword 검색 키워드
     * @return 검색 결과 게시글 목록
     */
    public List<PostResponseDto> searchPosts(String keyword) {
        List<Post> posts = postRepository.findByTitleContaining(keyword);
        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }
}