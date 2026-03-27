package project.food.global.api.kakao.local.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.food.global.api.kakao.local.dto.KakaoKeywordResponse;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoLocalService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private static final String KAKAO_KEYWORD_API_URL =
            "https://dapi.kakao.com/v2/local/search/keyword.json";

    private static final String CATEGORY_FOOD = "FD6";
    private static final int SIZE = 10;

    private final RestTemplate restTemplate;

    public KakaoKeywordResponse searchPlaceByKeyword(String keyword, int page) {
        long startedAt = System.currentTimeMillis();
        log.debug("[KAKAO][LOCAL] 키워드 검색 시작 keyword={} page={}", keyword, page);

        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", keyword)   // "강남맛집" 그대로 넣어도 됨
                .queryParam("page", page)
                .queryParam("size", 15)
                .encode(StandardCharsets.UTF_8) // 여기서 인코딩
                .build()
                .toUri();

        log.debug("[KAKAO][LOCAL] 호출 URL={}", uri);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoKeywordResponse> response =
                    restTemplate.exchange(uri, HttpMethod.GET, entity, KakaoKeywordResponse.class);

            KakaoKeywordResponse body = response.getBody();

            if (body == null || !body.hasResult()) {
                long elapsed = System.currentTimeMillis() - startedAt;
                log.warn("[KAKAO][LOCAL] 결과 없음 keyword={} page={} elapsedMs={}",
                        keyword, page, elapsed);
                throw new CustomException(ErrorCode.KAKAO_PLACE_NOT_FOUND);
            }

            int count = (body.getDocuments() == null) ? 0 : body.getDocuments().size();
            long elapsed = System.currentTimeMillis() - startedAt;

            log.info("[KAKAO][LOCAL] 성공 keyword={} page={} count={} elapsedMs={}",
                    keyword, page, count, elapsed);

            return body;

        } catch (CustomException e) {
            throw e;

        } catch (HttpClientErrorException e) {
            handleHttpError(e, "keyword", keyword);
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);

        } catch (RestClientException e) {
            long elapsed = System.currentTimeMillis() - startedAt;
            log.error("[KAKAO][LOCAL] 호출 오류 keyword={} page={} elapsedMs={} error={}",
                    keyword, page, elapsed, e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    private void handleHttpError(HttpClientErrorException e, String keyName, String keyValue) {
        HttpStatusCode status = e.getStatusCode();

        if (status == HttpStatus.UNAUTHORIZED) {
            log.error("[KAKAO][LOCAL] 인증 실패 {}={} status={} msg={}",
                    keyName, keyValue, status, e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_API_UNAUTHORIZED);
        }
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            log.error("[KAKAO][LOCAL] 호출 한도 초과 {}={} status={} msg={}",
                    keyName, keyValue, status, e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_API_RATE_LIMIT);
        }

        log.error("[KAKAO][LOCAL] HTTP 오류 {}={} status={} msg={}",
                keyName, keyValue, status, e.getMessage());
    }
}