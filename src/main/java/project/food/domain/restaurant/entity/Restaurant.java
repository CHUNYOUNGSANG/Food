package project.food.domain.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;
import project.food.global.common.BaseTimeEntity;

/**
 * 맛집 엔티티
 * - 카카오 장소 검색 API로 수집한 맛집 정보를 저장
 * - sourceId(카카오 place id)로 중복 저장 방지
 * - 이름/주소/카테고리에 인덱스를 걸어 검색 성능 확보
 */
@Entity
@Table(
        name = "restaurant",
        indexes = {
                @Index(name = "idx_restaurant_name", columnList = "name"),
                @Index(name = "idx_restaurant_address", columnList = "address"),
                @Index(name = "idx_restaurant_category", columnList = "category"),
                @Index(name = "uk_restaurant_source_id", columnList = "source_id", unique = true)
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Restaurant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 카카오 place id (외부 고유값, 중복 방지용)
    @Column(name = "source_id", length = 64, unique = true)
    private String sourceId;

    // 맛집 이름
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    // 주소 (도로명 주소 우선)
    @Column(name = "address", length = 500)
    private String address;

    // 카테고리 (한식, 일식, 카페 등)
    @Column(name = "category", length = 200)
    private String category;

    // 위도
    @Column(name = "latitude")
    private Double latitude;

    // 경도
    @Column(name = "longitude")
    private Double longitude;

    // 카카오 플레이스 URL
    @Column(name = "place_url", length = 500)
    private String placeUrl;
}