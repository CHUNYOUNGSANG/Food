package project.food.domain.like.postlike.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.food.domain.like.postlike.entity.PostLike;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    /**
     * 특정 회원이 특정 게시글에 좋아요 눌렀는지 확인
     * @param memberId
     * @param postId
     * @return 좋아요 존재 여부
     */
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

    /**
     * 특정 회원이 특정 게시글에 누른 좋아요 조회
     * Optional: null 체크를 통해 NullPointException 방지
     * @param memberId
     * @param postId
     * @return 좋아요 정보
     */
    Optional<PostLike> findByMemberIdAndPostId(Long memberId, Long postId);

    /**
     * 특정 게시글의 좋아요 개수 조회
     * @param postId
     * @return 좋아요 개수
     */
    Long countByPostId(Long postId);

    /**
     * 특정 회원이 좋아요한 게시글 목록 조회(생성 시간 순으로 내림정렬)
     * FETCH JOIN: N+1 문제 해결
     * @param memberId
     * @return 좋아요 목록(게시글 정보 포함)
     */
    @Query("SELECT pl FROM PostLike pl JOIN FETCH pl.post p JOIN FETCH p.member WHERE pl.member.id = :memberId ORDER BY pl.createdAt DESC")
    List<PostLike> findByMemberIdWithPost(@Param("memberId") Long memberId);

    /**
     * 특정 게시글의 좋아요 목록 조회(생성 시간 순으로 내림정렬)
     * @param postId
     * @return 좋아요 목록 조회(회원 정보 포함)
     */
    @Query("SELECT pl FROM PostLike pl JOIN FETCH pl.member WHERE pl.post.id = :postId ORDER BY pl.createdAt DESC")
    List<PostLike> findByPostIdWithPost(@Param("postId") Long postId);

    /**
     * 특정 회원의 모든 좋아요 삭제
     * @param memberId
     */
    void deleteByMemberId(Long memberId);

    /**
     * 특정 게시글의 모든 좋아요 삭제
     * @param postId
     */
    void deleteByPostId(Long postId);
}
