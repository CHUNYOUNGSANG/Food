package project.food.domain.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.comment.dto.CommentRequestDto;
import project.food.domain.comment.dto.CommentResponseDto;
import project.food.domain.comment.entity.Comment;
import project.food.domain.comment.repository.CommentRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

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

        // 1. 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        // 2. 회원 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 3. 대댓글인 경우 부모 댓글 검증
        if (requestDto.getParentCommentId() != null) {
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
        // 게시글 존재 확인
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findByPost_IdOrderByCreatedAtAsc(postId);

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
        if (!memberRepository.existsById(memberId)) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        List<Comment> comments = commentRepository.findByMember_IdAndDeletedFalseOrderByCreatedAtDesc(memberId);

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
        // 1. 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 2. 삭제된 댓글인지 확인
        if (comment.isDeleted()) {
            throw new CustomException(ErrorCode.COMMENT_ALREADY_DELETED);
        }

        // 3. 작성자 본인 확인
        if (!comment.isWriter(memberId)) {
            throw new CustomException(ErrorCode.COMMENT_AUTHOR_MISMATCH);
        }

        // 4. 댓글 내용 수정
        comment.updateContent(requestDto.getContent());

        return CommentResponseDto.from(comment);
    }

    /**
     * 댓글 삭제
     * <p>
     * 로직:
     * 1. 대댓글이 있는 경우 -> 삭제 (내용만 변경)
     * 2. 대댓글이 없는 경우
     * - 일반 댓글 -> 완전 삭제
     * - 대댓글 -> 완전 삭제 후 부모 댓글 정리
     *
     * @param commentId 댓글 ID
     * @param memberId  삭제 요청자 ID
     */
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        // 1. 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        // 2. 이미 삭제된 댓글인지 확인
        if (comment.isDeleted()) {
            throw new CustomException(ErrorCode.COMMENT_ALREADY_DELETED);
        }

        // 3. 작성자 본인 확인
        if (!comment.isWriter(memberId)) {
            throw new CustomException(ErrorCode.COMMENT_AUTHOR_MISMATCH);
        }

        // 4. 대댓글 존재 여부 확인
        boolean hasReplies = commentRepository.existsByParentCommentIdAndDeletedFalse(commentId);

        if (hasReplies) {
            // 4-1. 대댓글이 있으면 논리 삭제
            comment.softDelete();
        } else {
            // 4-2. 대댓글이 없으면 완전 삭제
            commentRepository.delete(comment);

            // 4-3. 대댓글이었다면 부모 댓글 정리
            if (comment.isReply()) {
                cleanupParentComment(comment.getParentCommentId());
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
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_PARENT_COMMENT));

        // 부모 댓글이 같은 게시글에 속하는지 확인
        if (!parentComment.getPostId().equals(postId)) {
            throw new CustomException(ErrorCode.INVALID_PARENT_COMMENT);
        }

        // 부모 댓글이 삭제되지 않았는지 확인
        if (parentComment.isDeleted()) {
            throw new CustomException(ErrorCode.INVALID_PARENT_COMMENT);
        }
    }

    /**
     * 부모 댓글 정리
     * - 부모 댓글이 삭제 상태이고
     * - 모든 대댓글이 삭제되었다면
     * - 부모 댓글도 완전히 삭제
     * @param parentCommentId 부모 댓글 ID
     */
    public void cleanupParentComment(Long parentCommentId) {
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElse(null);

        if (parentComment == null) {
            return;  // null이면 종료
        }

        // parentComment가 null이 아닐 때만 실행
        if (!parentComment.isDeleted()) {
            return;
        }

        boolean hasRemainingReplies = commentRepository.existsByParentCommentId(parentCommentId);

        if (!hasRemainingReplies) {
            commentRepository.delete(parentComment);
        }
    }
}
