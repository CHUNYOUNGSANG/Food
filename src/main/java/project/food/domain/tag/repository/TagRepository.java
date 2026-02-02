package project.food.domain.tag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.food.domain.tag.entity.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * 태그 이름으로 조회
     * @param name 태그이름
     * @return 이름
     */
    Optional<Tag> findByName(String name);

    /**
     * 태그 이름 중복 확인
     * @param name 중복 이름
     * @return 중복 이름
     */
    boolean existsByName(String name);

    /**
     * 이름에 키워드가 포함된 태그 검색
     * @param keyword 검색
     * @return 검색된 이름
     */
    List<Tag> findByNameContaining(String keyword);
}
