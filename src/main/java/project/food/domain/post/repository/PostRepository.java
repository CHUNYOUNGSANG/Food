package project.food.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.food.domain.post.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"member", "restaurant"})
    Page<Post> findByMemberId(Long memberId, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "restaurant"})
    Page<Post> findByTitleContaining(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "restaurant"})
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    Page<Post> findByRestaurant_IdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    Page<Post> findByRestaurantIsNull(Pageable pageable);

    @EntityGraph(attributePaths = {"member", "restaurant"})
    Optional<Post> findWithDetailsById(Long id);

    @Query("SELECT p.id FROM Post p WHERE p.member.id = :memberId")
    List<Long> findIdsByMemberId(@Param("memberId") Long memberId);

}
