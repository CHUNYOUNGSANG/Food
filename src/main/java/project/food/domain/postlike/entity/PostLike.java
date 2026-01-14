package project.food.domain.postlike.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.food.domain.member.entity.Member;
import project.food.domain.post.entity.Post;
import project.food.global.common.BaseTimeEntity;

@Entity
@Getter
@Table(name = "post_like",
uniqueConstraints = { // 유니크 제약조건
        @UniqueConstraint(
                name = "uk_post_like_member_post", // 제약조건 이름
                columnNames = {"member_id", "post_id"})}) // 복합 유니크 키
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseTimeEntity {

    /**
     * 좋아요 고유 번호(PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 좋아요를 누른 회원(FK)
     * ManyToOne(다대일): 여러 좋아요가 하나의 회원에 속함
     * LAZY(지연 로딩): 필요할 때만 정보 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 좋아요가 눌린 게시글(FK)
     * ManyToOne(다대일): 여러 좋아요가 하나의 게시글에 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    public PostLike(Member member, Post post) {
        this.member = member;
        this.post = post;
    }

    /**
     * 좋아요 여부 확인
     * @param memberId
     * @return 좋아요 소유 여부
     */
    public boolean isOwnedBy(Long memberId) {
        return this.member != null && this.member.getId().equals(memberId);
    }
}
