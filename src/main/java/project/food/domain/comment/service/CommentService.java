package project.food.domain.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.comment.dto.CommentRequestDto;
import project.food.domain.comment.dto.CommentResponseDto;
import project.food.domain.comment.entity.Comment;
import project.food.domain.comment.repository.CommentRepository;
import project.food.domain.like.commentlike.repository.CommentLikeRepository;
import project.food.domain.member.entity.Member;
import project.food.domain.member.repository.MemberRepository;
import project.food.domain.post.entity.Post;
import project.food.domain.post.repository.PostRepository;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Comment Service
 * - 댓글 비즈니스 로직 처리
 * - 권한 검증, 예외 처리 포함
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentLikeRepository commentLikeRepository;

    /**
     * 댓글 작성
     *
     * @param postId     게시판ID
     * @param memberId   회원ID
     * @param requestDto 댓글 내용
     * @return 생성된 댓글 정보
     */
    @Transactional
    public CommentResponseDto createComment(Long postId, Long memberId, CommentRequestDto requestDto) {

        log.debug("댓글 생성 시작: postId={}, memberId={}, isReply={}",
                postId, memberId, requestDto.getParentCommentId() != null);

        // 1. 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("❌ 게시글 찾기 실패: postId={}", postId);
                    return new CustomException(ErrorCode.POST_NOT_FOUND);
                });

        // 2. 회원 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("❌ 회원 찾기 실패: memberId={}", memberId);
                    return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
                });

        // 3. 대댓글인 경우 부모 댓글 검증
        if (requestDto.getParentCommentId() != null) {
            log.debug("대댓글 생성: parentCommentId={}", requestDto.getParentCommentId());
            validateParentComment(requestDto.getParentCommentId(), postId);
        }

        // 4. 댓글 생성 및 저장
        Comment comment = Comment.builder()
                .post(post)
                .member(member)
                .content(requestDto.getContent())
                .parentCommentId(requestDto.getParentCommentId())
                .build();

        Comment savedComment = commentRepository.save(comment);

        log.info("✅ 댓글 생성 완료: commentId={}, postId={}, memberId={}, isReply={}",
                savedComment.getId(), postId, memberId,
                requestDto.getParentCommentId() != null);

        return CommentResponseDto.from(savedComment);
    }

    /**
     * 특정 게시글의 댓글 목록 조회 (오래된 순)
     * - 삭제된 댓글도 포함 ("삭제된 댓글입니다"로 표시)
     *
     * @param postId 게시글 ID
     * @return 댓글 목록
     */
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {

        log.debug("게시글 댓글 목록 조회 시작: postId={}", postId);

        // 게시글 존재 확인
        if (!postRepository.existsById(postId)) {
            log.error("❌ 게시글 존재하지 않음: postId={}", postId);
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findByPost_IdOrderByCreatedAtAsc(postId);

        log.info("✅ 댓글 목록 조회 완료: postId={}, totalCount={}, deletedCount={}",
                postId, comments.size(),
                comments.stream().filter(Comment::isDeleted).count());

        return comments.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 회원의 댓글 목록 조회 (최신순)
     * - 삭제되지 않은 댓글만 조회
     *
     * @param memberId 회원 ID
     * @return 댓글 목록
     */
    public List<CommentResponseDto> getCommentsByMemberId(Long memberId) {

        log.debug("회원 댓글 목록 조회 시작: memberId={}", memberId);

        if (!memberRepository.existsById(memberId)) {
            log.error("❌ 회원 존재하지 않음: memberId={}", memberId);
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        List<Comment> comments = commentRepository
                .findByMember_IdAndDeletedFalseOrderByCreatedAtDesc(memberId);

        log.info("✅ 회원 댓글 목록 조회 완료: memberId={}, commentCount={}",
                memberId, comments.size());

        return comments.stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 댓글 수정
     *
     * @param commentId  댓글 ID
     * @param memberId   수정 요청자 ID
     * @param requestDto 수정 내용
     * @return 수정된 댓글 정보
     */
    @Transactional
    public CommentResponseDto updateComment(Long commentId, Long memberId, CommentRequestDto requestDto) {

        log.debug("댓글 수정 시작: commentId={}, memberId={}", commentId, memberId);

        // 1. 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("❌ 댓글 찾기 실패: commentId={}", commentId);
                    return new CustomException(ErrorCode.COMMENT_NOT_FOUND);
                });

        // 2. 삭제된 댓글인지 확인
        if (comment.isDeleted()) {
            log.warn("⚠️ 삭제된 댓글 수정 시도: commentId={}, memberId={}",
                    commentId, memberId);
            throw new CustomException(ErrorCode.COMMENT_ALREADY_DELETED);
        }

        // 3. 작성자 본인 확인
        if (!comment.isWriter(memberId)) {
            log.warn("⚠️ 댓글 수정 권한 없음: commentId={}, requestMemberId={}, writerMemberId={}",
                    commentId, memberId, comment.getMember().getId());
            throw new CustomException(ErrorCode.COMMENT_AUTHOR_MISMATCH);
        }

        // 4. 댓글 내용 수정
        String oldContent = comment.getContent();
        comment.updateContent(requestDto.getContent());

        log.info("✅ 댓글 수정 완료: commentId={}, memberId={}, contentChanged={}",
                commentId, memberId, !oldContent.equals(requestDto.getContent()));

        return CommentResponseDto.from(comment);
    }

    /**
     * 댓글 삭제
     * <p>
     * 로직:
     * 0. 댓글 좋아요 먼저 삭제 (외래키 제약조건)
     * 1. 대댓글이 있는 경우 -> 논리 삭제 (내용만 변경)
     * 2. 대댓글이 없는 경우
     * - 일반 댓글 -> 완전 삭제
     * - 대댓글 -> 완전 삭제 후 부모 댓글 정리
     *
     * @param commentId 댓글 ID
     * @param memberId  삭제 요청자 ID
     */
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {

        log.debug("댓글 삭제 시작: commentId={}, memberId={}", commentId, memberId);

        // 1. 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("❌ 댓글 찾기 실패: commentId={}", commentId);
                    return new CustomException(ErrorCode.COMMENT_NOT_FOUND);
                });

        // 2. 이미 삭제된 댓글인지 확인
        if (comment.isDeleted()) {
            log.warn("⚠️ 이미 삭제된 댓글 삭제 시도: commentId={}, memberId={}",
                    commentId, memberId);
            throw new CustomException(ErrorCode.COMMENT_ALREADY_DELETED);
        }

        // 3. 작성자 본인 확인
        Member requester = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!comment.isWriter(memberId) && !requester.isAdmin()) {
            log.warn("⚠️ 댓글 삭제 권한 없음: commentId={}, requestMemberId={}, writerMemberId={}",
                    commentId, memberId, comment.getMember().getId());
            throw new CustomException(ErrorCode.COMMENT_AUTHOR_MISMATCH);
        }

        // 4. 대댓글 존재 여부 확인
        boolean hasReplies = commentRepository
                .existsByParentCommentIdAndDeletedFalse(commentId);

        if (hasReplies) {
            // 4-1. 대댓글이 있으면 논리 삭제 (좋아요는 유지)
            comment.softDelete();
            log.info("✅ 댓글 논리 삭제 완료 (대댓글 있음): commentId={}, memberId={}",
                    commentId, memberId);
        } else {
            // 4-2. 대댓글이 없으면 완전 삭제
            Long parentCommentId = comment.getParentCommentId();
            boolean isReply = comment.isReply();

            // ⭐ 댓글 좋아요 먼저 삭제 (외래키 제약조건)
            commentLikeRepository.deleteByCommentId(commentId);
            log.debug("댓글 좋아요 삭제 완료: commentId={}", commentId);

            commentRepository.delete(comment);
            log.info("✅ 댓글 완전 삭제 완료: commentId={}, memberId={}, isReply={}",
                    commentId, memberId, isReply);

            // 4-3. 대댓글이었다면 부모 댓글 정리
            if (isReply) {
                log.debug("부모 댓글 정리 시작: parentCommentId={}", parentCommentId);
                cleanupParentComment(parentCommentId);
            }
        }
    }


    /**
     * private 헬퍼 메서드
     */

    /**
     * 부모 댓글 검증
     * - 부모 댓글 존재 확인
     * - 부모 댓글이 같은 게시글에 속하지는 확인
     *
     * @param parentCommentId 부모 댓글 ID
     * @param postId          게시글 ID
     */
    private void validateParentComment(Long parentCommentId, Long postId) {

        log.debug("부모 댓글 검증 시작: parentCommentId={}, postId={}",
                parentCommentId, postId);

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> {
                    log.error("❌ 부모 댓글 찾기 실패: parentCommentId={}", parentCommentId);
                    return new CustomException(ErrorCode.INVALID_PARENT_COMMENT);
                });

        // 부모 댓글이 같은 게시글에 속하는지 확인
        if (!parentComment.getPostId().equals(postId)) {
            log.error("❌ 부모 댓글이 다른 게시글에 속함: parentCommentId={}, " +
                            "expectedPostId={}, actualPostId={}",
                    parentCommentId, postId, parentComment.getPostId());
            throw new CustomException(ErrorCode.INVALID_PARENT_COMMENT);
        }

        // 부모 댓글이 삭제되지 않았는지 확인
        if (parentComment.isDeleted()) {
            log.error("❌ 삭제된 댓글에 대댓글 작성 시도: parentCommentId={}",
                    parentCommentId);
            throw new CustomException(ErrorCode.INVALID_PARENT_COMMENT);
        }

        log.debug("✅ 부모 댓글 검증 완료: parentCommentId={}", parentCommentId);
    }

    /**
     * 부모 댓글 정리
     * - 부모 댓글이 삭제 상태이고
     * - 모든 대댓글이 삭제되었다면
     * - 부모 댓글도 완전히 삭제
     * @param parentCommentId 부모 댓글 ID
     */
    public void cleanupParentComment(Long parentCommentId) {

        log.debug("부모 댓글 정리 시작: parentCommentId={}", parentCommentId);

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElse(null);

        if (parentComment == null) {
            log.debug("부모 댓글 없음 (이미 삭제됨): parentCommentId={}", parentCommentId);
            return;
        }

        if (!parentComment.isDeleted()) {
            log.debug("부모 댓글이 삭제 상태 아님 (정리 불필요): parentCommentId={}",
                    parentCommentId);
            return;
        }

        boolean hasRemainingReplies = commentRepository
                .existsByParentCommentId(parentCommentId);

        if (!hasRemainingReplies) {
            commentRepository.delete(parentComment);
            log.info("✅ 부모 댓글 완전 삭제 완료 (모든 대댓글 삭제됨): parentCommentId={}",
                    parentCommentId);
        } else {
            log.debug("부모 댓글 정리 불필요 (대댓글 남아있음): parentCommentId={}",
                    parentCommentId);
        }
    }
}
