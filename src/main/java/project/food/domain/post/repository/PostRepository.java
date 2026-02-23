package project.food.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import project.food.domain.post.entity.Post;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    /**
     * 특정 회원이 작성한 게시글 목록 조회
     *
     * @param memberId 회원 Id
     * @return 게시글 목록
     */
    List<Post> findByMemberId(Long memberId);

    /**
     * keyword 검색 키워드
     * @param keyword 검색 키워드
     * @return 게시글 목록
     */
    List<Post> findByTitleContaining(String keyword);

    /**
     * 최신순 정렬
     * @return 최신 게시글 목록
     */
    List<Post> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"member"})
    Page<Post> findByRestaurant_IdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    Page<Post> findByRestaurantIsNull(Pageable pageable);

}
