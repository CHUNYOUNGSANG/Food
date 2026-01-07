package project.food.domain.post.entity;

import jakarta.persistence.*;
import lombok.*;
import project.food.domain.member.entity.Member;

import java.math.BigDecimal;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = {"member"})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "restaurant_name", length = 100, nullable = false)
    private String restaurantName;

    @Column(name = "restaurant_address", length = 300)
    private String restaurantAddress;

    @Column(name = "food_category", length = 50)
    private String foodCategory;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewcount = 0;

    public void updatePost(String title, String content, String restaurantName,
                           String restaurantAddress, String foodCategory,
                           BigDecimal rating, String imageUrl) {
        this.title = title;
        this.content = content;
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.foodCategory = foodCategory;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }
}
