package project.food.domain.tag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.food.domain.tag.entity.PostTag;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    /**
     * 특정 게시글의 태그 목록 조회
     * @param postId 게시글 ID
     * @return 특정 게시글의 태그 목록
     */
    List<PostTag> findByPostId(Long postId);

    /**
     * 특정 태그가 달린 게시글 목록 조회
     * @param tagId 태그 ID
     * @return 특정 태그가 달린 게시글 목록
     */
    List<PostTag> findByTagId(Long tagId);

    /**
     * 게시글 - 태그 중복 확인
     * @param postId 게시글 ID
     * @param tagId 태그 ID
     * @return 태그 중복 확인
     */
    boolean existsByPostIdAndTagId(Long postId, Long tagId);

    /**
     * 특정 게시글의 태그 전체 삭제
     * @param postId 게시글 ID
     */
    void deleteByPostId(Long postId);
}
