package project.food.domain.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import project.food.domain.restaurant.entity.Restaurant;
import project.food.domain.restaurant.repository.RestaurantRepository;
import project.food.domain.tag.entity.PostTag;
import project.food.domain.tag.entity.Tag;
import project.food.domain.tag.repository.TagRepository;
import project.food.domain.like.postlike.repository.PostLikeRepository;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;
import project.food.global.file.dto.UploadedFileInfo;
import project.food.global.file.service.FileStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
    private final FileStorage fileStorageService;
    private final RestaurantRepository restaurantRepository;
    private final TagRepository tagRepository;
    private final PostLikeRepository postLikeRepository;

    /**
     * 게시글 생성
     * @param memberId 작성자 ID
     * @param request 게시글 생성 요청 데이터
     * @return 생성된 게시글 정보
     */
    @Transactional
    public PostResponseDto createPost(Long memberId, PostRequestDto request) {

        log.debug("게시글 생성 시작: memberId={}, title={}", memberId, request.getTitle());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("회원 찾기 실패: memberId={}", memberId);
                    return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
                });

        // 음식점 조회 또는 카카오 맛집 자동 저장
        Restaurant restaurant = null;
        if (request.getRestaurantId() != null) {
            // 기존 맛집 ID로 연결
            restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> {
                        log.error("음식점 찾기 실패: restaurantId={}", request.getRestaurantId());
                        return new CustomException(ErrorCode.RESTAURANT_NOT_FOUND);
                    });
        } else if (request.getPlaceId() != null) {
            // 카카오 placeId로 중복 체크 → 없으면 새로 저장
            restaurant = restaurantRepository.findBySourceId(request.getPlaceId())
                    .orElseGet(() -> restaurantRepository.save(
                            Restaurant.builder()
                                    .sourceId(request.getPlaceId())
                                    .name(request.getPlaceName())
                                    .address(request.getPlaceAddress())
                                    .category(request.getPlaceCategory())
                                    .latitude(request.getPlaceLatitude())
                                    .longitude(request.getPlaceLongitude())
                                    .placeUrl(request.getPlaceUrl())
                                    .build()
                    ));
            log.debug("카카오 맛집 연결: placeId={}, restaurantId={}", request.getPlaceId(), restaurant.getId());
        }

        Post post = Post.builder()
                .member(member)
                .title(request.getTitle())
                .content(request.getContent())
                .restaurant(restaurant)
                .rating(request.getRating())
                .build();

        Post savedPost = postRepository.save(post);
        log.debug("게시글 저장 완료: postId={}", savedPost.getId());

        // 이미지 저장
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            log.debug("이미지 저장 시작: postId={}, imageCount={}",
                    savedPost.getId(), request.getImages().size());
            savePostImages(savedPost, request.getImages());
        }

        // 태그 저장
        if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
            log.debug("태그 저장 시작: postId={}, tagCount={}",
                    savedPost.getId(), request.getTagNames().size());
            savePostTags(savedPost, request.getTagNames());
        }

        log.info("게시글 생성 완료: postId={}, memberId={}, title={}, imageCount={}, tagCount={}",
                savedPost.getId(), memberId, savedPost.getTitle(),
                savedPost.getImages().size(), savedPost.getPostTags().size());

        return PostResponseDto.from(savedPost);
    }

    /**
     * 게시글 전체 목록 조회 (최신순)
     * @return 게시글 목록
     */
    public Page<PostResponseDto> getAllPosts(Pageable pageable) {

        log.debug("게시글 전체 목록 조회 시작");

        Page<PostResponseDto> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(PostResponseDto::from);

        log.info("게시글 전체 목록 조회 완료: totalCount={}", posts.getTotalElements());

        return posts;
    }

    /**
     * 게시글 상세 조회
     * 조회수 증가
     * @param postId 게시글 ID
     * @return 게시글 상세 정보
     */
    @Transactional
    public PostResponseDto getPost(Long postId) {

        log.debug("게시글 상세 조회 시작: postId={}", postId);

        Post post = postRepository.findWithDetailsById(postId)
                .orElseThrow(() -> {
                    log.error("게시글 찾기 실패: postId={}", postId);
                    return new CustomException(ErrorCode.POST_NOT_FOUND);
                });

        int beforeViewCount = post.getViewCount();
        post.increaseViewCount();

        log.debug("조회수 증가: postId={}, {} → {}",
                postId, beforeViewCount, post.getViewCount());

        log.info("게시글 상세 조회 완료: postId={}, title={}, viewCount={}",
                postId, post.getTitle(), post.getViewCount());

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

        log.debug("게시글 수정 시작: postId={}, memberId={}", postId, memberId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글 찾기 실패: postId={}", postId);
                    return new CustomException(ErrorCode.POST_NOT_FOUND);
                });

        if (!post.isWriter(memberId)) {
            log.warn("게시글 수정 권한 없음: postId={}, requestMemberId={}, writerMemberId={}",
                    postId, memberId, post.getMember().getId());
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }

        // 게시글 기본 정보 수정
        post.updatePost(
                request.getTitle(),
                request.getContent(),
                request.getRating()
        );

        // 음식점 변경 (기존 ID 또는 카카오 맛집 새로 등록)
        if (request.getRestaurantId() != null) {
            // 기존 맛집 ID로 변경
            Long currentRestaurantId = post.getRestaurant() != null ? post.getRestaurant().getId() : null;
            if (!Objects.equals(currentRestaurantId, request.getRestaurantId())) {
                Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                        .orElseThrow(() -> {
                            log.error("음식점 찾기 실패: restaurantId={}", request.getRestaurantId());
                            return new CustomException(ErrorCode.RESTAURANT_NOT_FOUND);
                        });
                post.assignRestaurant(restaurant);
                log.debug("음식점 변경: postId={}, newRestaurantId={}", postId, request.getRestaurantId());
            }
        } else if (request.getPlaceId() != null) {
            // 카카오 placeId로 중복 체크 → 없으면 새로 저장
            Restaurant restaurant = restaurantRepository.findBySourceId(request.getPlaceId())
                    .orElseGet(() -> restaurantRepository.save(
                            Restaurant.builder()
                                    .sourceId(request.getPlaceId())
                                    .name(request.getPlaceName())
                                    .address(request.getPlaceAddress())
                                    .category(request.getPlaceCategory())
                                    .latitude(request.getPlaceLatitude())
                                    .longitude(request.getPlaceLongitude())
                                    .placeUrl(request.getPlaceUrl())
                                    .build()
                    ));
            post.assignRestaurant(restaurant);
            log.debug("카카오 맛집 변경: postId={}, placeId={}, restaurantId={}",
                    postId, request.getPlaceId(), restaurant.getId());
        }

        log.debug("게시글 정보 수정 완료: postId={}", postId);

        // 삭제할 이미지 처리
        if (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
            log.debug("이미지 삭제 시작: postId={}, deleteCount={}",
                    postId, request.getDeleteImageIds().size());
            deletePostImages(post, request.getDeleteImageIds());
        }

        // 새 이미지 추가
        if (request.getNewImages() != null && !request.getNewImages().isEmpty()) {
            log.debug("새 이미지 추가 시작: postId={}, newImageCount={}",
                    postId, request.getNewImages().size());
            savePostImages(post, request.getNewImages());
        }

        // 태그 수정 (tagNames가 null이면 태그 수정 안 함)
        if (request.getTagNames() != null) {
            // 현재 태그 이름 목록
            List<String> currentTagNames = post.getPostTags().stream()
                    .map(pt -> pt.getTag().getName())
                    .toList();

            List<String> newTagNames = request.getTagNames().stream()
                    .distinct().toList();

            // 태그가 변경된 경우에만 처리 (같으면 스킵)
            if (!new HashSet<>(currentTagNames).equals(new HashSet<>(newTagNames))) {
                log.debug("태그 수정 시작: postId={}, before={}, after={}",
                        postId, currentTagNames, newTagNames);
                post.clearPostTag();
                postRepository.flush(); // DELETE를 먼저 DB에 반영 (유니크 제약 충돌 방지)
                if (!newTagNames.isEmpty()) {
                    savePostTags(post, newTagNames);
                }
            }
        }

        log.info("게시글 수정 완료: postId={}, memberId={}, currentImageCount={}, tagCount={}",
                postId, memberId, post.getImages().size(), post.getPostTags().size());

        return PostResponseDto.from(post);

    }

    /**
     * 게시글 삭제
     * @param postId 게시글 ID
     * @param memberId 삭제 요청자 ID
     */
    @Transactional
    public void deletePost(Long postId, Long memberId) {

        log.debug("게시글 삭제 시작: postId={}, memberId={}", postId, memberId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("게시글 찾기 실패: postId={}", postId);
                    return new CustomException(ErrorCode.POST_NOT_FOUND);
                });

        Member requester = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!post.isWriter(memberId) && !requester.isAdmin()) {
            log.warn("게시글 삭제 권한 없음: postId={}, requestMemberId={}, writerMemberId={}",
                    postId, memberId, post.getMember().getId());
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }

        List<String> filePaths = post.getImages().stream()
                .map(PostImage::getFilePath)
                .collect(Collectors.toList());

        if (!filePaths.isEmpty()) {
            log.debug("게시글 이미지 파일 삭제 시작: postId={}, imageCount={}",
                    postId, filePaths.size());
            fileStorageService.deleteFiles(filePaths);
            log.debug("게시글 이미지 파일 삭제 완료: postId={}", postId);
        }

        postLikeRepository.deleteByPostId(postId);

        postRepository.delete(post);

        log.info("게시글 삭제 완료: postId={}, memberId={}, deletedImages={}",
                postId, memberId, filePaths.size());
    }

    /**
     * 특정 회원의 게시글 목록 조회
     * @param memberId 회원 ID
     * @return 해당 회원의 게시글 목록
     */
    public Page<PostResponseDto> getPostsByMember(Long memberId, Pageable pageable) {

        log.debug("회원 게시글 목록 조회 시작: memberId={}", memberId);

        Page<PostResponseDto> posts = postRepository.findByMemberId(memberId, pageable)
                .map(PostResponseDto::from);

        log.info("회원 게시글 목록 조회 완료: memberId={}, postCount={}",
                memberId, posts.getTotalElements());

        return posts;
    }

    /**
     * 게시글 검색 (제목)
     * @param keyword 검색 키워드
     * @return 검색 결과 게시글 목록
     */
    public Page<PostResponseDto> searchPosts(String keyword, Pageable pageable) {

        log.debug("게시글 검색 시작: keyword={}", keyword);

        Page<PostResponseDto> posts = postRepository.findByTitleContaining(keyword, pageable)
                .map(PostResponseDto::from);

        log.info("게시글 검색 완료: keyword={}, resultCount={}",
                keyword, posts.getTotalElements());

        return posts;
    }

    /**
     * 게시글 태그 저장
     * - 태그가 없으면 새로 생성, 있으면 기존 태그 사용
     * @param post 게시글
     * @param tagNames 태그 이름 목록
     */
    private void savePostTags(Post post, List<String> tagNames) {
        List<String> distinctTagNames = tagNames.stream()
                .distinct()
                .collect(Collectors.toList());

        for (String tagName : distinctTagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(
                            Tag.builder().name(tagName).build()
                    ));

            PostTag postTag = PostTag.builder()
                    .post(post)
                    .tag(tag)
                    .build();

            post.addPostTag(postTag);
        }

        log.info("태그 저장 완료: postId={}, tagCount={}", post.getId(), distinctTagNames.size());
    }

    /**
     * 게시글 이미지 저장
     * @param post 게시글
     * @param imageFiles 이미지파일
     */
    private void savePostImages(Post post, List<MultipartFile> imageFiles) {

        log.debug("이미지 파일 저장 시작: postId={}, fileCount={}",
                post.getId(), imageFiles.size());

        // 파일 저장
        List<UploadedFileInfo> uploadedFiles = fileStorageService.storeFiles(imageFiles, post.getMember().getId());

        log.debug("파일 스토리지 저장 완료: postId={}, savedCount={}",
                post.getId(), uploadedFiles.size());

        // PostImage 엔티티 생성
        int displayOrder = post.getImages().size();
        int savedCount = 0;

        for (UploadedFileInfo fileInfo : uploadedFiles) {
            PostImage postImage = PostImage.builder()
                    .post(post)
                    .originalFileName(fileInfo.getOriginalFileName())
                    .storedFileName(fileInfo.getStoredFileName())
                    .filePath(fileInfo.getFileUrl())
                    .fileSize(fileInfo.getFileSize())
                    .displayOrder(displayOrder++)
                    .build();

            post.addImage(postImage);
            postImageRepository.save(postImage);
            savedCount++;

            log.debug("이미지 엔티티 저장: postImageId={}, originalFileName={}, fileSize={}",
                    postImage.getId(), fileInfo.getOriginalFileName(), fileInfo.getFileSize());
        }

        log.info("이미지 저장 완료: postId={}, savedImageCount={}, totalImageCount={}",
                post.getId(), savedCount, post.getImages().size());
    }

    /**
     * 게시글 이미지 삭제
     * @param post 게시글
     * @param imageIds 이미지 ID
     */
    private void deletePostImages(Post post, List<Long> imageIds) {

        log.debug("이미지 삭제 시작: postId={}, deleteImageIds={}", post.getId(), imageIds);

        int deletedCount = 0;

        for (Long imageId : imageIds) {
            PostImage postImage = postImageRepository.findById(imageId)
                    .orElseThrow(() -> {
                        log.error("이미지 찾기 실패: imageId={}", imageId);
                        return new CustomException(ErrorCode.FILE_NOT_FOUND);
                    });

            // 게시글 소유 확인
            if (!postImage.getPost().getId().equals(post.getId())) {
                log.warn("이미지가 다른 게시글에 속함: imageId={}, requestPostId={}, actualPostId={}",
                        imageId, post.getId(), postImage.getPost().getId());
                throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
            }

            String filePath = postImage.getFilePath();

            // 파일 삭제
            fileStorageService.deleteFile(filePath);
            log.debug("파일 삭제 완료: filePath={}", filePath);

            // 엔티티 삭제
            post.removeImage(postImage);
            postImageRepository.delete(postImage);
            deletedCount++;

            log.debug("이미지 엔티티 삭제 완료: imageId={}, originalFileName={}",
                    imageId, postImage.getOriginalFileName());
        }

        log.info("이미지 삭제 완료: postId={}, deletedCount={}, remainingCount={}",
                post.getId(), deletedCount, post.getImages().size());
    }
}
