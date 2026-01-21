package project.food.domain.like.postlike.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.member.entity.Member;
import project.food.domain.member.repository.MemberRepository;
import project.food.domain.post.entity.Post;
import project.food.domain.post.repository.PostRepository;
import project.food.domain.like.postlike.dto.PostLikeCountDTO;
import project.food.domain.like.postlike.dto.PostLikeResponseDto;
import project.food.domain.like.postlike.entity.PostLike;
import project.food.domain.like.postlike.repository.PostLikeRepository;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    /**
     * 좋아요 추가
     * @param memberId
     * @param postId
     * @return 생성된 좋아요 정보
     */
    @Transactional
    public PostLikeResponseDto addLike(Long memberId, Long postId) {
        log.info("좋아요 추가: memberId = {}, postId = {}", memberId, postId);

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 좋아요 여부 확인
        if (postLikeRepository.existsByMemberIdAndPostId(memberId, postId)) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }
        
        // 좋아요 생성 및 저장
        PostLike postLike = PostLike.builder()
                .member(member)
                .post(post)
                .build();

        PostLike savedLike = postLikeRepository.save(postLike);
        
        log.info("좋아요 추가 완료: likeId = {}", savedLike.getId());

        return PostLikeResponseDto.simple(savedLike);
    }

    /**
     * 좋아요 취소
     * @param memberId
     * @param postId
     */
    @Transactional
    public void removeLike(Long memberId, Long postId) {
        log.info("좋아요 취소: memberId = {}, postId = {}", memberId, postId);

        // 좋아요 조회
        PostLike postLike = postLikeRepository.findByMemberIdAndPostId(memberId, postId)
                .orElseThrow(() -> new CustomException(ErrorCode.LIKE_NOT_FOUND));

        // 좋아요 삭제
        postLikeRepository.delete(postLike);

        log.info("좋아요 취소 완료: likeId = {}", postLike.getId());
    }

    /**
     * 좋아요 토글 (있으면 취소, 없으면 추가)
     * @param memberId
     * @param postId
     * @return 좋아요 추가 여부 (true: 추가, false: 취소)
     */
    @Transactional
    public boolean toggleLike(Long memberId, Long postId) {
        log.info("좋아요 토글: memberId = {}, postId = {}", memberId, postId);

        boolean exists = postLikeRepository.existsByMemberIdAndPostId(memberId, postId);

        if (exists) {
            // 좋아요가 있으면 취소
            removeLike(memberId, postId);
            return false;
        } else {
            // 좋아요가 없으면 추가
            addLike(memberId, postId);
            return true;
        }
    }

    /**
     * 게시글의 좋아요 개수 및 사용자의 좋아요 여부 조회
     * @param memberId
     * @param postId
     * @return 좋아요 개수 및 좋아요 여부
     */
    public PostLikeCountDTO getLikeCount(Long memberId, Long postId) {
        log.info("좋아요 개수 조회: memberId = {}, postId = {}", memberId, postId);

        // 게시글 존재 확인
        if (!postLikeRepository.existsById(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        // 좋아요 개수 조회
        Long likeCount = postLikeRepository.countByPostId(postId);

        // 사용자의 좋아요 여부 확인
        Boolean isLiked = false;
        if (memberId != null) {
            isLiked = postLikeRepository.existsByMemberIdAndPostId(memberId, postId);
        }

        return PostLikeCountDTO.builder()
                .postId(postId)
                .likeCount(likeCount)
                .isLiked(isLiked)
                .build();
    }

    /**
     * 회원이 좋아요한 게시글 목록 조회
     * @param memberId
     * @return 좋아요한 게시글 목록
     */
    public List<PostLikeResponseDto> getLikedPostsByMember(Long memberId) {
        log.info("회원이 좋아요한 게시글 목록 조회: memberId = {}", memberId);

        // 회원 존재 확인
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 좋아요 목록 조회 (Fetch Join)
        List<PostLike> likes = postLikeRepository.findByMemberIdWithPost(memberId);

        return likes.stream()
                .map(PostLikeResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<PostLikeResponseDto> getLikesByPost(Long postId) {
        log.info("게시글의 좋아요 목록 조회: postId = {}", postId);

        // 게시글의 존재 확인
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        // 좋아요 목록 조회 (Fetch Join)
        List<PostLike> likes = postLikeRepository.findByPostIdWithPost(postId);

        return likes.stream()
                .map(PostLikeResponseDto::from)
                .collect(Collectors.toList());
    }
}
