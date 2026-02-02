package project.food.domain.tag.entity;

import jakarta.persistence.*;
import lombok.*;
import project.food.global.common.BaseTimeEntity;

/**
 * 태그 엔티티
 * - 게시글에 붙일 수 있는 태그 관리
 */
@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    /**
     * 태그 이름 수정
     * @param name 수정할 이름
     */
    public void updateName(String name) {
        this.name = name;
    }
}
