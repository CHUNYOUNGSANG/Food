package project.food.domain.comment.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.food.domain.comment.entity.Comment;

import java.util.List;

/**
 * Comment Repository
 * - 댓글 데이터 접근 계층
 * - Spring Data JPA가 자동으로 구현체 생성
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
    /**
     * 특정 게시글의 댓글 목록 조회 (오래된 순)
     * - 삭제된 댓글도 포함
     *
     * @param postId 게시글 ID
     * @return 댓글 목록 (오래된 댓글이 먼저)
     */
    @EntityGraph(attributePaths = {"member"})
    List<Comment> findByPost_IdOrderByCreatedAtAsc(Long postId);

    /**
     * 특정 회원이 작성한 댓글 목록 조회 (최신순)
     * - 삭제되지 않은 댓글만
     *
     * @param memberId 회원 ID
     * @return 댓글 목록 (최신 댓글이 먼저)
     */
    @EntityGraph(attributePaths = {"member"})
    List<Comment> findByMember_IdAndDeletedFalseOrderByCreatedAtDesc(Long memberId);

    /**
     * 특정 부모 댓글에 대댓글이 있는지 확인
     * @param parentCommentId 부모 댓글 ID
     * @return 대댓글 존재 여부
     */
    boolean existsByParentCommentId(Long parentCommentId);

    /**
     * 특정 부모 댓글에 삭제되지 않은 대댓글 있는지 확인
     * @param parentCommentId 부모 댓글 ID
     * @return 존재 여부
     */
    boolean existsByParentCommentIdAndDeletedFalse(Long parentCommentId);

    /**
     * 특정 회원이 작성한 모든 댓글 삭제 (회원 탈퇴 시 사용)
     * @param memberId
     */
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

}
