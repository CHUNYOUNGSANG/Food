package project.food.domain.like.commentlike.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.food.domain.like.commentlike.entity.CommentLike;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    /**
     * 특정 회원이 특정 댓글에 좋아요 눌렀는지 확인
     * @param memberId
     * @param commentId
     * @return true: 좋아요 누름, false: 좋아요 안 누름
     */
    boolean existsByMemberIdAndCommentId(Long memberId, Long commentId);

    /**
     * 특정 회원이 특정 댓글에 누른 좋아요 조회
     * @param memberId
     * @param commentId
     * @return 있으면 정보, 없으면 empty
     */
    Optional<CommentLike> findByMemberIdAndCommentId(Long memberId, Long commentId);

    /**
     * 특정 댓글의 좋아요 개수 조회
     * @param commentId
     * @return 좋아요 개수
     */
    Long countByCommentId(Long commentId);

    /**
     * 특정 회원이 좋아요한 댓글 목록 조회 (내가 좋아요한 댓글 목록)
     * FETCH JOIN: Comment와 Post, Member 정보 함께 조회
     * @param memberId
     * @return 좋아요 목록 (최신순)
     */
    @Query("SELECT cl FROM CommentLike cl " +
            "JOIN FETCH cl.comment c " +
            "JOIN FETCH c.post p " +
            "JOIN FETCH c.member m " +
            "WHERE cl.member.id = :memberId " +
            "ORDER BY cl.createdAt DESC")
    List<CommentLike> findByMemberIdWithComment(@Param("memberId") Long memberId);

    /**
     * 특정 댓글의 좋아요 목록 조회 (누가 좋아요 눌렀는지 확인)
     * FETCH JOIN: Member 정보를 함께 조회
     * @param commentId
     * @return 좋아요 목록 (최신순)
     */
    @Query("SELECT cl FROM CommentLike cl " +
            "JOIN FETCH cl.member " +
            "WHERE cl.comment.id = :commentId " +
            "ORDER BY cl.createdAt DESC")
    List<CommentLike> findByCommentIdWithMember(@Param("commentId") Long commentId);

    /**
     * 특정 회원의 모든 좋아요 삭제 (회원 탈퇴 시 사용)
     * @param memberId
     */
    void deleteByMemberId(Long memberId);

    /**
     * 특정 댓글의 모든 좋아요 삭제 (댓글 삭제 시 사용)
     * @param commentId
     */
    void deleteByCommentId(Long commentId);

    /**
     * 특정 게시글에 달린 댓글의 좋아요 일괄 삭제 (게시글 삭제 시 사용)
     * @param postId 게시글 ID
     */
    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.comment.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    /**
     * 여러 게시글에 달린 댓글의 좋아요 일괄 삭제 (회원 탈퇴 시 사용)
     * @param postIds 게시글 ID 목록
     */
    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.comment.post.id IN :postIds")
    void deleteByPostIdIn(@Param("postIds") List<Long> postIds);

    /**
     * 특정 회원이 작성한 댓글에 달린 모든 좋아요 삭제 (회원 탈퇴 시 사용)
     * @param memberId
     */
    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.comment.member.id = :memberId")
    void deleteByCommentMemberId(@Param("memberId") Long memberId);
}
