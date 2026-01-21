package project.food.domain.post.repository;

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
     * 음식 카테고리로 게시글 목록 조회
     *
     * @param foodCategory 음식 카테고리
     * @return 게시글 목록
     */
    List<Post> findByFoodCategory(String foodCategory);

    /**
     * keyword 검색 키워드
     * @param keyword 검색 키워드
     * @return 게시글 목록
     */
    List<Post> findByTitleContaining(String keyword);

    /**
     * 최신술 정렬
     * @return 최신 게시글 목록
     */
    List<Post> findAllByOrderByCreatedAtDesc();




}
