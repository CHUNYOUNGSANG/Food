package project.food.domain.comment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.food.domain.member.entity.Member;
import project.food.domain.post.entity.Post;
import project.food.global.common.BaseTimeEntity;

/**
 * Comment 엔티티
 *
 * 기능:
 * - 게시글에 대한 작성
 * - 댓글에 대댓글 작성
 * - 댓글 수정/삭제 (작성자 권환)
 *
 * 연관간계:
 * - Member (N:1) - 작성자
 * - Post (N:1) - 게시글
 * - Comment - 대댓글 구조
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comments")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 댓글 내용
     * - 논리 삭제 시: "삭제된 댓글입니다"
     */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * 부모 댓글 ID (대댓글 구조)
     */
    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    /**
     * 삭제 여부
     * - false: 일반 댓글
     * - true: 논리 삭제된 댓글
     */
    @Column(nullable = false)
    private boolean deleted =false;

    @Builder
    public Comment(Post post, Member member, String content, Long parentCommentId) {
        this.post = post;
        this.member = member;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.deleted = false;
    }

    /**
     * 댓글 내용 수정
     * @param content
     */
    public void updateContent(String content) {
        if(this.deleted) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다.");
        }
        this.content = content;
    }

    /**
     * 작성자 확인
     * @param memberId
     * @return memberId
     */
    public boolean isWriter(Long memberId) {
        return this.member.getId().equals(memberId);
    }

    /**
     * 대댓글 여부 확인
     * @return 부모ID
     */
    public boolean isReply() {
        return this.parentCommentId != null;
    }

    public void softDelete() {
        this.content = "삭제된 댓글입니다.";
        this.deleted = true;
    }

    /**
     * 삭제 여부 확인
     * @return
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    public Long getPostId() {
        return this.post.getId();
    }

    public Long getMemberId() {
        return this.member.getId();
    }

    public String getMemberNickname() {
        return this.member.getNickname();
    }


}
