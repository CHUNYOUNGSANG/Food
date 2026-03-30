package project.food.global.api.naver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverSearchService {

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    private static final String NAVER_IMAGE_SEARCH_URL = "https://openapi.naver.com/v1/search/image";

    private final RestTemplate restTemplate;

    /**
     * 음식점 이름으로 네이버 이미지 검색 후 첫 번째 이미지 byte[] 반환
     * 이미지가 없거나 실패하면 null 반환 (실패해도 맛집 저장은 계속 진행)
     */
    public byte[] searchRestaurantImage(String restaurantName) {
        log.debug("[NAVER][IMAGE] 이미지 검색 시작: query={}", restaurantName);

        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(NAVER_IMAGE_SEARCH_URL)
                    .queryParam("query", restaurantName)
                    .queryParam("display", 1)   // 첫 번째 이미지만
                    .queryParam("sort", "sim")  // 유사도순
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    uri, HttpMethod.GET, entity, Map.class);

            Map body = response.getBody();
            if (body == null) return null;

            List<Map> items = (List<Map>) body.get("items");
            if (items == null || items.isEmpty()) {
                log.warn("[NAVER][IMAGE] 검색 결과 없음: query={}", restaurantName);
                return null;
            }

            // thumbnail: 작은 미리보기 / link: 원본 이미지
            String imageUrl = (String) items.get(0).get("thumbnail");
            if (imageUrl == null) return null;

            log.debug("[NAVER][IMAGE] 이미지 URL 획득: query={}, url={}", restaurantName, imageUrl);

            // 이미지 다운로드
            return downloadImage(imageUrl);

        } catch (Exception e) {
            log.warn("[NAVER][IMAGE] 이미지 검색 실패 (스킵): query={}, error={}", restaurantName, e.getMessage());
            return null;
        }
    }

    /**
     * 이미지 URL로 byte[] 다운로드
     */
    private byte[] downloadImage(String imageUrl) {
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(imageUrl, byte[].class);
            log.debug("[NAVER][IMAGE] 이미지 다운로드 완료: size={} bytes",
                    response.getBody() != null ? response.getBody().length : 0);
            return response.getBody();
        } catch (Exception e) {
            log.warn("[NAVER][IMAGE] 이미지 다운로드 실패 (스킵): url={}, error={}", imageUrl, e.getMessage());
            return null;
        }
    }
}