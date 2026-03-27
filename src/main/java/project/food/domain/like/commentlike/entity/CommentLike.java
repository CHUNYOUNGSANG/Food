package project.food.domain.like.commentlike.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.food.domain.comment.entity.Comment;
import project.food.domain.member.entity.Member;
import project.food.global.common.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "comment_like",
uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_comment_like_member_comment",
                columnNames = {"member_id", "comment_id"})
})
public class CommentLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 좋아요를 누른 회원(FK)
     * 여러 좋아요가 하나의 회원에 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    /**
     * 좋아요가 눌린 댓글(FK)
     * 여러 좋아요가 하나의 댓글에 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Comment comment;

    @Builder
    public CommentLike(Member member, Comment comment) {
        this.member = member;
        this.comment = comment;
    }

    /**
     * 좋아요 소유 여부 확인
     * @param memberId
     * @return true: 본인의 좋아요, false: 타인의 좋아요
     */
    public boolean isOwnedBy(Long memberId) {
        return this.member != null && this.member.getId().equals(memberId);
    }
}
