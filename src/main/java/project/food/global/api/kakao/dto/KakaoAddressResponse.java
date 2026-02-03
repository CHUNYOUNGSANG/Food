package project.food.global.api.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoAddressResponse {

    private List<Document> documents;
    private Meta meta;

    @Getter
    @NoArgsConstructor
    public static class Document {
        private String addressName;     // 전체 주소
        private String x;               // 경도
        private String y;               // 위도

    }

    @Getter
    @NoArgsConstructor
    public static class Meta {
        @JsonProperty("total_count")
        private Integer totalCount;
    }

    /**
     * 좌표 추출 헬퍼 메서드
     */
    public Double getLatitude() {
        if (documents == null || documents.isEmpty()) {
            return null;
        }
        return Double.parseDouble(documents.get(0).getY());
    }

    public Double getLongitude() {
        if (documents == null || documents.isEmpty()) {
            return null;
        }
        return Double.parseDouble(documents.get(0).getX());
    }

    public boolean hasResult() {
        return documents != null && !documents.isEmpty();
    }
}
