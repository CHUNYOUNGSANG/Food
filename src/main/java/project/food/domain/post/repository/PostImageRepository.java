package project.food.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.food.domain.post.entity.PostImage;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    /**
     * 게시글 ID로 이미지 목록 조회
     * - 표시 순서대로 정렬
     * @param postId 게시글 ID
     * @return 이미지
     */
    List<PostImage> findByPostIdOrderByDisplayOrderAsc(Long postId);

    /**
     * 게시글 ID로 이미지 삭제
     * @param postId 게시글 ID
     */
    void deleteByPostId(Long postId);

    /**
     * 게시글 ID로 이미지 개수 조회
     * @param postId 게시글 ID
     * @return 이미지 개수
     */
    int countByPostId(Long postId);
}
