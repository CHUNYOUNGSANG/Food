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

    // Cloudflare 등 봇 차단이 강한 도메인 - 다운로드 시도 제외
    private static final List<String> BLOCKED_DOMAINS = List.of(
            "i.namu.wiki", "namu.wiki", "cloudflare"
    );

    /**
     * 음식점 이름으로 네이버 이미지 검색 후 다운로드 가능한 첫 이미지 byte[] 반환
     * 이미지가 없거나 실패하면 null 반환 (실패해도 맛집 저장은 계속 진행)
     */
    public byte[] searchRestaurantImage(String restaurantName) {
        log.debug("[NAVER][IMAGE] 이미지 검색 시작: query={}", restaurantName);

        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(NAVER_IMAGE_SEARCH_URL)
                    .queryParam("query", restaurantName)
                    .queryParam("display", 5)   // 여러 개 받아서 순차 시도
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

            // 차단된 도메인은 스킵하고 다운로드 가능한 URL 순차 시도
            for (Map item : items) {
                String imageUrl = (String) item.get("link");
                if (imageUrl == null) continue;
                if (BLOCKED_DOMAINS.stream().anyMatch(imageUrl::contains)) continue;

                byte[] imageBytes = downloadImage(imageUrl);
                if (imageBytes != null) return imageBytes;
            }

            log.warn("[NAVER][IMAGE] 다운로드 가능한 이미지 없음: query={}", restaurantName);
            return null;

        } catch (Exception e) {
            log.warn("[NAVER][IMAGE] 이미지 검색 실패 (스킵): query={}, error={}", restaurantName, e.getMessage());
            return null;
        }
    }

    /**
     * 이미지 URL로 byte[] 다운로드 (User-Agent 헤더 추가로 봇 차단 우회)
     */
    private byte[] downloadImage(String imageUrl) {
        try {
            HttpHeaders downloadHeaders = new HttpHeaders();
            downloadHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            HttpEntity<Void> downloadEntity = new HttpEntity<>(downloadHeaders);

            ResponseEntity<byte[]> response = restTemplate.exchange(imageUrl, HttpMethod.GET, downloadEntity, byte[].class);
            log.debug("[NAVER][IMAGE] 이미지 다운로드 완료: size={} bytes",
                    response.getBody() != null ? response.getBody().length : 0);
            return response.getBody();
        } catch (Exception e) {
            log.warn("[NAVER][IMAGE] 이미지 다운로드 실패 (스킵): url={}, error={}", imageUrl, e.getMessage());
            return null;
        }
    }
}