package project.food.global.api.kakao.local.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 카카오 키워드 장소 검색 응답 DTO
 * - 음식점 키워드 검색 결과를 매핑
 */
@Getter
@NoArgsConstructor
public class KakaoKeywordResponse {

    private List<Place> documents;
    private Meta meta;

    @Getter
    @NoArgsConstructor
    public static class Place {

        private String id;

        @JsonProperty("place_name")
        private String placeName;

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("road_address_name")
        private String roadAddressName;

        private String x;
        private String y;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("place_url")
        private String placeUrl;
    }

    @Getter
    @NoArgsConstructor
    public static class Meta {
        @JsonProperty("total_count")
        private Integer totalCount;

        @JsonProperty("is_end")
        private Boolean isEnd;
    }

    public boolean hasResult() {
        return documents != null && !documents.isEmpty();
    }
}