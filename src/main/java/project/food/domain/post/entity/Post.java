package project.food.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import project.food.domain.comment.entity.Comment;
import project.food.domain.member.entity.Member;
import project.food.domain.tag.entity.PostTag;
import project.food.domain.tag.entity.Tag;
import project.food.global.common.BaseTimeEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 게시글 엔티티
 * - 맛집 리뷰 게시글 정보를 관리
 * - BaseTimeEntity 상속으로 createdAt, updatedAt 자동관리
 */
@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = {"member"})
public class Post extends BaseTimeEntity {
    /**
     * 게시글 고유 번호 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 작성자 정보 (Foreign Key)
     * - ManyToOne: 여러 게시글이 한 회원에게 속함
     * - LAZY: 필요할 때만 회원 정보 조회
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /**
     * 게시글 제목
     */
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    /**
     * 게시글 내용
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 맛집 이름
     */
    @Column(name = "restaurant_name", length = 100, nullable = false)
    private String restaurantName;

    /**
     * 맛집 주소
     */
    @Column(name = "restaurant_address", length = 300)
    private String restaurantAddress;

    /**
     * 음식 카테고리
     * - 예: 한식, 중식, 일식, 양식 등
     */
    @Column(name = "food_category", length = 50)
    private String foodCategory;

    /**
     * 평점
     * - 작성자가 매긴 평점
     * - 0.0 ~ 5.0 범위
     */
    @Column(name = "rating")
    private Double rating;

    /**
     * 조회수
     * - 기본값 0
     */
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    /**
     * 맛집 위도
     * - 지도 표시용 좌표
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * 맛집 경도
     * - 지도 표시용 좌표
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * 댓굴 목록 (1:N)
     * - 게시글 삭제 시 댓글도 함께 삭제 (cascade = ALl)
     * - 연관관계가 끊긴 댓글 자동 삭제 (orphanRemoval = true)
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    /**
     * 이미지
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")    // 표시 순서대로 정렬
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();

    /**
     * 태그 목록 (1:N)
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostTag> postTags = new ArrayList<>();

    /**
     * 게시글 수정
     * @param title 수정할 제목
     * @param content 수정할 내용
     * @param restaurantName 수정할 맛집 이름
     * @param restaurantAddress 수정할 맛집 주소
     * @param foodCategory 수정할 음식 카테고리
     * @param rating 수정할 평점
     * @param latitude 수정할 위도
     * @param longitude 수정할 경도
     */

    public void updatePost(String title, String content, String restaurantName,
                           String restaurantAddress, String foodCategory,
                           Double rating, Double latitude, Double longitude) {
        this.title = title;
        this.content = content;
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.foodCategory = foodCategory;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * 태그 추가
     * @param postTag 태그
     */
    public void addPostTag(PostTag postTag) {
        this.postTags.add(postTag);
    }

    /**
     * 태그 제거
     * @param postTag 태그
     */
    public void removePostTag(PostTag postTag) {
        this.postTags.remove(postTag);
    }

    /**
     * 태그 전체 제거
     */
    public void clearPostTag() {
        this.postTags.clear();
    }

    /**
     * 이미지 추가
     * 양방향 연관관계 설정
     * @param image 이미지
     */
    public void addImage(PostImage image) {
        this.images.add(image);
        image.setPost(this);
    }

    /**
     * 이미지 제거
     * 양방향 연관관계 해제
     * @param image 이미지
     */
    public void removeImage(PostImage image) {
        this.images.remove(image);
    }

    /**
     * 모든 이미지 제거
     */
    public void clearImages() {
        this.images.clear();
    }

    /**
     * 이미지 개수 조회
     * @return 이미지 개수
     */
    public int getImageCount() {
        return this.images.size();
    }

    /**
     * 대표 이미지 조회
     * @return 첫 번째 이미지
     */
    public PostImage getThumbnailImage() {
        return this.images.isEmpty() ? null : this.images.get(0);
    }

    /**
     * 조회수 증가
     * - 게시글 조회 시 호출
     */
    public void increaseViewCount() {
        this.viewCount++;
    }

    /**
     * 작성자 확인
     * 게시글 수정/삭제 권한 확인 시 사용
     */
    public boolean isWriter(Long memberId) {
        return this.member != null && this.member.getId().equals(memberId);
    }

    /**
     * 평점 문자열 반환
     * - 화면 표시용
     * @return 평점 문자열 (예: "4.5점")
     */
    public String getRatingAsString() {
        return this.rating != null ?
                String.format("%.1f점", this.rating) : "평점 없음";
    }
    /**
     * 댓글 개수 조회
     * @return 댓글 개수
     */
    public int getCommentsCount() {
        return this.comments.size();
    }

    /**
     * 댓글 추가
     * (양방향 연관관계 설정)
     * @param comment 추가할 댓글
     */
    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    /**
     * 댓글 제거
     * (양방향 연관관계 해제)
     * @param comment 제거할 댓글
     */
    public void removeComment(Comment comment) {
        this.comments.remove(comment);
    }

    /**
     * 좌표 정보 존재 여부 확인
     * @return 좌표가 모두 있으면 true
     */
    public boolean hasCoordinates() {
        return this.latitude != null && this.longitude != null;
    }

    /**
     * 좌표 업데이트
     * - 주소 변경 시 카카오 API 조회 후 호출
     * @param latitude 위도
     * @param longitude 경도
     */
    public void updateCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
