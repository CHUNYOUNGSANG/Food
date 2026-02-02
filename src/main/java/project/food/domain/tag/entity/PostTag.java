package project.food.domain.tag.entity;

import jakarta.persistence.*;
import lombok.*;
import project.food.domain.post.entity.Post;
import project.food.global.common.BaseTimeEntity;

/**
 * 게시글-태그 연관관계 엔티티
 * - Post와 Tag의 다대다(N:M) 관계를 중간 테이블로 관리
 */
@Entity
@Table(name = "post_tag", uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_tag", columnNames = {"post_id", "tag_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}