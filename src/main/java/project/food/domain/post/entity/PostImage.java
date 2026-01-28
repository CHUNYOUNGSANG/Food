package project.food.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import project.food.global.common.BaseTimeEntity;

/**
 * 게시글 이미지 엔티티
 * - 게시글에 첨부된 이미지 정보 관리
 * - Post 와 N:1 관계
 */
@Entity
@Table(name = "post_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = {"post"})
public class PostImage extends BaseTimeEntity {

    /**
     * 이미지 고유 번호 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 게시글 정보 (Foreign Key)
     * - ManyToOne: 여러 이미지가 한 게시글에 속함
     * - LAZY: 필요할 때만 게시글 정보 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /**
     * 원본 파일명
     * - 사용자가 업로드한 파일의 원래 이름
     */
    @Column(name = "original_file_name", length = 255, nullable = false)
    private String originalFileName;

    /**
     * 저장된 파일명
     * - 서버에 실제로 저장된 파일 이름 (UUID 등으로 생성)
     * - 파일명 중복 방지
     */
    @Column(name = "stored_file_name", length = 255, nullable = false)
    private String storedFileName;

    /**
     * 파일 경로
     * - 파일이 저장된 전체 경로 또는 URL
     */
    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;

    /**
     * 파일 크기 (bytes)
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 이미지 표시 순서
     * - 게시글에서 이미지 표시 순서 지정
     * - 0부터 시작
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * 게시글 연관관계 설정
     * - 양방향 연관관계
     * @param post 게시글
     */
    public void setPost(Post post) {
        this.post = post;
        post.getImages().add(this);
    }

    /**
     * 표시 순서 변경
     * @param displayOrder 표시순서
     */
    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
