package project.food.domain.like.commentlike.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.comment.entity.Comment;
import project.food.domain.comment.repository.CommentRepository;
import project.food.domain.like.commentlike.dto.CommentLikeCountDto;
import project.food.domain.like.commentlike.dto.CommentLikeResponseDto;
import project.food.domain.like.commentlike.entity.CommentLike;
import project.food.domain.like.commentlike.repository.CommentLikeRepository;
import project.food.domain.member.entity.Member;
import project.food.domain.member.repository.MemberRepository;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    /**
     * 댓글 좋아요 추가
     * @param memberId
     * @param commentId
     * @return 생성된 좋아요 정보
     */
    @Transactional
    public CommentLikeResponseDto addLike(Long memberId, Long commentId) {
        log.info("댓글 좋아요 추가: memberId = {}, commentId = {}", memberId, commentId);

        // 회원 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 중복 좋아요 확인
        if (commentLikeRepository.existsByMemberIdAndCommentId(memberId, commentId)) {
            log.warn("중복 좋아요 시도: memberId = {}, commentId = {}", memberId, commentId);
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        // 좋아요 생성
        CommentLike commentLike = CommentLike.builder()
                .member(member)
                .comment(comment)
                .build();

        CommentLike saveLike = commentLikeRepository.save(commentLike);

        log.info("댓글 좋아요 추가 완료: likeId = {}, memberId = {}, commentId = {}",
                saveLike.getId(), memberId, commentId);

        // 간단한 정보 반환 (성공 확인용)
        return CommentLikeResponseDto.simple(saveLike);
    }

    /**
     * 댓글 좋아요 취소
     * @param memberId
     * @param commentId
     */
    @Transactional
    public void removeLike(Long memberId, Long commentId) {
        log.info("댓글 좋아요 취소: memberId = {}, commentId = {}", memberId, commentId);

        // 좋아요 조회
        CommentLike commentLike = commentLikeRepository
                .findByMemberIdAndCommentId(memberId, commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.LIKE_NOT_FOUND));

        // 좋아요 삭제
        commentLikeRepository.delete(commentLike);

        log.info("댓글 좋아요 취소 완료: likeId = {}, memberId = {}, commentId = {}",
                commentLike.getId(), memberId, commentId);
    }

    /**
     * 댓글 좋아요 토글 (있으면 취소, 없으면 추가)
     * @param memberId
     * @param commentId
     * @return true: 추가, false: 취소
     */
    @Transactional
    public boolean likeToggle(Long memberId, Long commentId) {
        log.info("댓글 좋아요 토글: memberId = {}, commentId = {}", memberId, commentId);

        // 좋아요 존재 여부 확인
        boolean exists = commentLikeRepository.existsByMemberIdAndCommentId(memberId, commentId);

        if (exists) {
            // 좋아요가 있으면 취소
            removeLike(memberId, commentId);
            log.info("댓글 좋아요 토글 취소: memberId = {}, commentId = {}", memberId, commentId);
            return false; // 좋아요 취소
        } else {
            // 좋아요가 없으면 추가
            addLike(memberId, commentId);
            log.info("댓글 좋아요 토글 추가: memberId = {}, commentId = {}", memberId, commentId);
            return true; // 좋아요 추가
        }
    }

    /**
     * 댓글 좋아요 개수 및 사용자의 좋아요 여부 확인
     * @param memberId
     * @param commentId
     * @return CommentLikeCountDto (개수 + 좋아요 여부)
     */
    public CommentLikeCountDto getLikeCount(Long memberId, Long commentId) {
        log.info("댓글 좋아요 개수 조회: memberId = {}, commentId = {}", memberId, commentId);

        // 댓글 존재 확인
        if (!commentRepository.existsById(commentId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 좋아요 개수 조회
        Long likeCount = commentLikeRepository.countByCommentId(commentId);

        // 사용자의 좋아요 여부 확인
        Boolean isLiked = false;
        if (memberId != null) {
            isLiked = commentLikeRepository.existsByMemberIdAndCommentId(memberId, commentId);
        }
        log.info("댓글 좋아요 개수 조회 완료: commentId = {}, likeCount = {}, isLiked = {}",
                commentId, likeCount, isLiked);

        return CommentLikeCountDto.builder()
                .commentId(commentId)
                .likeCount(likeCount)
                .isLiked(isLiked)
                .build();
    }

    /**
     * 특정 회원이 좋아요한 댓글 목록 조회
     * FETCH JOIN: Comment, Post, Member 정보를 함께 조회
     *
     * @param memberId
     * @return 좋아요한 댓글 목록 (최신순)
     */
    public List<CommentLikeResponseDto> getLikedCommentsByMember(Long memberId) {
        log.info("회원이 좋아요한 댓글 목록 조회: memberId = {}", memberId);

        // 회원 존재 확인
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 좋아요 목록 조회 (FETCH JOIN)
        List<CommentLike> likes = commentLikeRepository.findByMemberIdWithComment(memberId);

        log.info("회원이 좋아요한 댓글 목록 조회 완료: memberId = {}, count = {}",
                memberId, likes.size());

        // Entity -> DTO 변환
        return likes.stream()
                .map(CommentLikeResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 댓글의 좋아요 목록 조회 (누가 좋아요 눌렀는지)
     * - "좋아요 10명" -> 좋아요 누른 사람 목록 표시
     * FETCH JOIN: Member 정보를 함께 조회
     *
     * @param commentId
     * @return 좋아요 목록 (최신순, 회원 정보 포함)
     */
    public List<CommentLikeResponseDto> getLikesByComment(Long commentId) {
        log.info("댓글의 좋아요 목록 조회: commentId = {}", commentId);

        // 댓글 존재 확인
        if (!commentRepository.existsById(commentId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        // 좋아요 목록 조회
        List<CommentLike> likes = commentLikeRepository.findByCommentIdWithMember(commentId);

        log.info("댓글의 좋아요 목록 조회 완료: commentId = {}, count = {}",
                commentId, likes.size());

        // Entity -> DTO 변환
        return likes.stream()
                .map(CommentLikeResponseDto::from)
                .collect(Collectors.toList());
    }
}
