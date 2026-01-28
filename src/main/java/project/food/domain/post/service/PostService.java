package project.food.domain.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.food.domain.member.entity.Member;
import project.food.domain.member.repository.MemberRepository;
import project.food.domain.post.dto.PostRequestDto;
import project.food.domain.post.dto.PostResponseDto;
import project.food.domain.post.dto.PostUpdateDto;
import project.food.domain.post.entity.Post;
import project.food.domain.post.entity.PostImage;
import project.food.domain.post.repository.PostImageRepository;
import project.food.domain.post.repository.PostRepository;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;
import project.food.global.file.dto.UploadedFileInfo;
import project.food.global.file.service.FileStorageService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Post Service
 * Global Exception 구조 사용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostImageRepository postImageRepository;
    private final FileStorageService fileStorageService;

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
                .build();

        Post savedPost = postRepository.save(post);

        // 이미지 저장
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            savePostImages(savedPost, request.getImages());
        }

        log.info("✅ 게시글 생성 완료: postId={}, memberId={}, imageCount={}",
                savedPost.getId(), memberId, savedPost.getImages().size());

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
                request.getRating()
        );

        // 삭제할 이미지 처리
        if (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
            deletePostImages(post, request.getDeleteImageIds());
        }

        // 새 이미지 추가
        if (request.getNewImages() != null && !request.getNewImages().isEmpty()) {
            savePostImages(post, request.getNewImages());
        }

        log.info("✅ 게시글 수정 완료: postId={}, currentImageCount={}",
                postId, post.getImages().size());

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

        List<String> storedFileNames = post.getImages().stream()
                        .map(PostImage::getStoredFileName)
                        .collect(Collectors.toList());

        if (!storedFileNames.isEmpty()) {
            fileStorageService.deleteFiles(storedFileNames);
        }

        postRepository.delete(post);

        log.info("✅ 게시글 삭제 완료: postId={}, deletedImages={}",
                postId, storedFileNames.size());
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

    /**
     * 게시글 이미지 저장
     * @param post 게시글
     * @param imageFiles 이미지파일
     */
    private void savePostImages(Post post, List<MultipartFile> imageFiles) {
        // 파일저장
        List<UploadedFileInfo> uploadedFiles = fileStorageService.storeFiles(imageFiles);

        // PostImage 엔티티 생성
        int displayOrder = post.getImages().size();

        for (UploadedFileInfo fileInfo : uploadedFiles) {
            PostImage postImage = PostImage.builder()
                    .post(post)
                    .originalFileName(fileInfo.getOriginalFileName())
                    .storedFileName(fileInfo.getStoredFileName())
                    .filePath(fileInfo.getFilePath())
                    .fileSize(fileInfo.getFileSize())
                    .displayOrder(displayOrder++)
                    .build();

            post.addImage(postImage);
            postImageRepository.save(postImage);
        }
        log.info("✅ 이미지 저장 완료: postId={}, imageCount={}",
                post.getId(), uploadedFiles.size());
    }

    /**
     * 게시글 이미지 삭네
     * @param post 게시글
     * @param imageIds 이미지 ID
     */
    private void deletePostImages(Post post, List<Long> imageIds) {
        for (Long imageId : imageIds) {
            PostImage postImage = postImageRepository.findById(imageId)
                    .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

            // 게시글 소유 확인
            if (!postImage.getPost().getId().equals(post.getId())) {
                throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
            }

            // 파일 삭제
            fileStorageService.deleteFile(postImage.getOriginalFileName());

            // 엔티티 삭제
            post.removeImage(postImage);
            postImageRepository.delete(postImage);
        }

        log.info("✅ 이미지 삭제 완료: postId={}, deletedCount={}",
                post.getId(), imageIds.size());
    }
}