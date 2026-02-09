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
import project.food.domain.tag.entity.PostTag;
import project.food.domain.tag.entity.Tag;
import project.food.domain.tag.repository.TagRepository;
import project.food.global.api.kakao.dto.KakaoAddressResponse;
import project.food.global.api.kakao.service.KakaoMapService;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;
import project.food.global.file.dto.UploadedFileInfo;
import project.food.global.file.service.FileStorageService;

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
    private final FileStorageService fileStorageService;
    private final KakaoMapService kakaoMapService;
    private final TagRepository tagRepository;

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
                    log.error("❌ 회원 찾기 실패: memberId={}", memberId);
                    return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
                });

        Double latitude = null;
        Double longitude = null;

        if (request.getRestaurantAddress() != null && !request.getRestaurantAddress().isBlank()) {
            try {
                KakaoAddressResponse response = kakaoMapService
                        .getCoordinateByAddress(request.getRestaurantAddress());

                latitude = response.getLatitude();
                longitude = response.getLongitude();

                log.debug("✅좌표 조회 성공: address={}, lat={}, lon={}",
                        request.getRestaurantAddress(), latitude, longitude);

            } catch (Exception e) {
                log.warn("❌좌표 조회 실패 (게시글 생성은 계속): address={}, error={}",
                        request.getRestaurantAddress(), e.getMessage());
            }
        }

        Post post = Post.builder()
                .member(member)
                .title(request.getTitle())
                .content(request.getContent())
                .restaurantName(request.getRestaurantName())
                .restaurantAddress(request.getRestaurantAddress())
                .latitude(latitude)
                .longitude(longitude)
                .foodCategory(request.getFoodCategory())
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

        log.info("✅ 게시글 생성 완료: postId={}, memberId={}, title={}, imageCount={}, tagCount={}",
                savedPost.getId(), memberId, savedPost.getTitle(),
                savedPost.getImages().size(), savedPost.getPostTags().size());

        return PostResponseDto.from(savedPost);
    }

    /**
     * 게시글 전체 목록 조회 (최신순)
     * @return 게시글 목록
     */
    public List<PostResponseDto> getAllPosts() {

        log.debug("게시글 전체 목록 조회 시작");

        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();

        log.info("✅ 게시글 전체 목록 조회 완료: totalCount={}", posts.size());

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

        log.debug("게시글 상세 조회 시작: postId={}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("❌ 게시글 찾기 실패: postId={}", postId);
                    return new CustomException(ErrorCode.POST_NOT_FOUND);
                });

        int beforeViewCount = post.getViewCount();
        post.increaseViewCount();

        log.debug("조회수 증가: postId={}, {} → {}",
                postId, beforeViewCount, post.getViewCount());

        log.info("✅ 게시글 상세 조회 완료: postId={}, title={}, viewCount={}",
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
                    log.error("❌ 게시글 찾기 실패: postId={}", postId);
                    return new CustomException(ErrorCode.POST_NOT_FOUND);
                });

        if (!post.isWriter(memberId)) {
            log.warn("⚠️ 게시글 수정 권한 없음: postId={}, requestMemberId={}, writerMemberId={}",
                    postId, memberId, post.getMember().getId());
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }

        Double newLatitude = post.getLatitude();
        Double newLongitude = post.getLongitude();

        boolean addressChanged = !Objects.equals(post.getRestaurantAddress(), request.getRestaurantAddress());

        if (addressChanged && request.getRestaurantAddress() != null) {
            try {
                KakaoAddressResponse response = kakaoMapService
                        .getCoordinateByAddress(request.getRestaurantAddress());

                newLatitude = response.getLatitude();
                newLongitude = response.getLongitude();

                log.debug("✅좌표 재조회 성공: newAddress={}, lat={}, lon={}",
                        request.getRestaurantAddress(), newLatitude, newLongitude);

            } catch (Exception e) {
                log.warn("❌좌표 재조회 실패: address={}, error={}",
                        request.getRestaurantAddress(), e.getMessage());
            }
        }

        String oldTitle = post.getTitle();
        post.updatePost(
                request.getTitle(),
                request.getContent(),
                request.getRestaurantName(),
                request.getRestaurantAddress(),
                request.getFoodCategory(),
                request.getRating(),
                newLatitude,
                newLongitude
        );

        log.debug("게시글 정보 수정 완료: postId={}, titleChanged={}",
                postId, !oldTitle.equals(request.getTitle()));

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

        // 태그 수정 (전체 교체)
        if (request.getTagNames() != null) {
            log.debug("태그 수정 시작: postId={}, newTagCount={}",
                    postId, request.getTagNames().size());
            post.clearPostTag();
            if (!request.getTagNames().isEmpty()) {
                savePostTags(post, request.getTagNames());
            }
        }

        log.info("✅ 게시글 수정 완료: postId={}, memberId={}, currentImageCount={}, tagCount={}",
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
                    log.error("❌ 게시글 찾기 실패: postId={}", postId);
                    return new CustomException(ErrorCode.POST_NOT_FOUND);
                });

        if (!post.isWriter(memberId)) {
            log.warn("⚠️ 게시글 삭제 권한 없음: postId={}, requestMemberId={}, writerMemberId={}",
                    postId, memberId, post.getMember().getId());
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }

        List<String> storedFileNames = post.getImages().stream()
                .map(PostImage::getStoredFileName)
                .collect(Collectors.toList());

        if (!storedFileNames.isEmpty()) {
            log.debug("게시글 이미지 파일 삭제 시작: postId={}, imageCount={}",
                    postId, storedFileNames.size());
            fileStorageService.deleteFiles(storedFileNames);
            log.debug("게시글 이미지 파일 삭제 완료: postId={}", postId);
        }

        postRepository.delete(post);

        log.info("✅ 게시글 삭제 완료: postId={}, memberId={}, deletedImages={}",
                postId, memberId, storedFileNames.size());
    }

    /**
     * 특정 회원의 게시글 목록 조회
     * @param memberId 회원 ID
     * @return 해당 회원의 게시글 목록
     */
    public List<PostResponseDto> getPostsByMember(Long memberId) {

        log.debug("회원 게시글 목록 조회 시작: memberId={}", memberId);

        List<Post> posts = postRepository.findByMemberId(memberId);

        log.info("✅ 회원 게시글 목록 조회 완료: memberId={}, postCount={}",
                memberId, posts.size());

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

        log.debug("카테고리별 게시글 조회 시작: category={}", foodCategory);

        List<Post> posts = postRepository.findByFoodCategory(foodCategory);

        log.info("✅ 카테고리별 게시글 조회 완료: category={}, postCount={}",
                foodCategory, posts.size());

        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 주소로 좌표 조회
     * - 실패 시 null 반환 (예외 발생 X)
     *
     * @param address 주소
     * @return 좌표 배열 [latitude, longitude] 또는 [null, null]
     */
    private Double[] getCoordinates(String address) {
        if (address == null || address.isBlank()) {
            return new Double[]{null, null};
        }

        try {
            KakaoAddressResponse response = kakaoMapService
                    .getCoordinateByAddress(address);

            Double latitude = response.getLatitude();
            Double longitude = response.getLongitude();

            log.debug("✅좌표 조회 성공: address={}, lat={}, lng={}",
                    address, latitude, longitude);

            return new Double[]{latitude, longitude};

        } catch (Exception e) {
            log.warn("❌좌표 조회 실패: address={}, error={}",
                    address, e.getMessage());
            return new Double[]{null, null};
        }
    }

    /**
     * 게시글 검색 (제목)
     * @param keyword 검색 키워드
     * @return 검색 결과 게시글 목록
     */
    public List<PostResponseDto> searchPosts(String keyword) {

        log.debug("게시글 검색 시작: keyword={}", keyword);

        List<Post> posts = postRepository.findByTitleContaining(keyword);

        log.info("✅ 게시글 검색 완료: keyword={}, resultCount={}",
                keyword, posts.size());

        return posts.stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 태그 저장
     * - 태그가 없으면 새로 생성, 있으면 기존 태그 사용
     * @param post 게시글
     * @param tagNames 태그 이름 목록
     */
    private void savePostTags(Post post, List<String> tagNames) {
        for (String tagName : tagNames) {
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

        log.info("✅ 태그 저장 완료: postId={}, tagCount={}", post.getId(), tagNames.size());
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
        List<UploadedFileInfo> uploadedFiles = fileStorageService.storeFiles(imageFiles);

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
                    .filePath(fileInfo.getFilePath())
                    .fileSize(fileInfo.getFileSize())
                    .displayOrder(displayOrder++)
                    .build();

            post.addImage(postImage);
            postImageRepository.save(postImage);
            savedCount++;

            log.debug("이미지 엔티티 저장: postImageId={}, originalFileName={}, fileSize={}",
                    postImage.getId(), fileInfo.getOriginalFileName(), fileInfo.getFileSize());
        }

        log.info("✅ 이미지 저장 완료: postId={}, savedImageCount={}, totalImageCount={}",
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
                        log.error("❌ 이미지 찾기 실패: imageId={}", imageId);
                        return new CustomException(ErrorCode.FILE_NOT_FOUND);
                    });

            // 게시글 소유 확인
            if (!postImage.getPost().getId().equals(post.getId())) {
                log.warn("⚠️ 이미지가 다른 게시글에 속함: imageId={}, requestPostId={}, actualPostId={}",
                        imageId, post.getId(), postImage.getPost().getId());
                throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
            }

            String storedFileName = postImage.getStoredFileName();

            // 파일 삭제
            fileStorageService.deleteFile(storedFileName);
            log.debug("파일 삭제 완료: storedFileName={}", storedFileName);

            // 엔티티 삭제
            post.removeImage(postImage);
            postImageRepository.delete(postImage);
            deletedCount++;

            log.debug("이미지 엔티티 삭제 완료: imageId={}, originalFileName={}",
                    imageId, postImage.getOriginalFileName());
        }

        log.info("✅ 이미지 삭제 완료: postId={}, deletedCount={}, remainingCount={}",
                post.getId(), deletedCount, post.getImages().size());
    }
}