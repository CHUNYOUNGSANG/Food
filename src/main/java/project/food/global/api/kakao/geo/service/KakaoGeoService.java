package project.food.global.api.kakao.geo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import project.food.global.api.kakao.geo.dto.KakaoAddressResponse;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;


/**
 * 카카오 지도 API 서비스
 * 주소 -> 좌표
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoGeoService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private static final String KAKAO_ADDRESS_API_URL =
            "https://dapi.kakao.com/v2/local/search/address.json";

    private final RestTemplate restTemplate;

    public KakaoAddressResponse getCoordinateByAddress(String address) {
        long startedAt = System.currentTimeMillis();
        log.debug("[KAKAO][GEO] 주소 검색 시작 address={}", address);

        String uri = UriComponentsBuilder
                .fromHttpUrl(KAKAO_ADDRESS_API_URL)
                .queryParam("query", address)
                .build(true)
                .toUriString();

        log.debug("[KAKAO][GEO] 호출 URL={}", uri);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoAddressResponse> response =
                    restTemplate.exchange(uri, HttpMethod.GET, entity, KakaoAddressResponse.class);

            KakaoAddressResponse body = response.getBody();

            if (body == null || !body.hasResult()) {
                long elapsed = System.currentTimeMillis() - startedAt;
                log.warn("[KAKAO][GEO] 결과 없음 address={} elapsedMs={}", address, elapsed);
                throw new CustomException(ErrorCode.KAKAO_ADDRESS_NOT_FOUND);
            }

            long elapsed = System.currentTimeMillis() - startedAt;
            log.info("✅ [KAKAO][GEO] 성공 address={} lat={} lng={} elapsedMs={}",
                    address, body.getLatitude(), body.getLongitude(), elapsed);

            return body;

        } catch (CustomException e) {
            throw e;

        } catch (HttpClientErrorException e) {
            handleHttpError(e, "address", address);
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);

        } catch (RestClientException e) {
            long elapsed = System.currentTimeMillis() - startedAt;
            log.error("⚠️ [KAKAO][GEO] 호출 오류 address={} elapsedMs={} error={}",
                    address, elapsed, e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    private void handleHttpError(HttpClientErrorException e, String keyName, String keyValue) {
        HttpStatusCode status = e.getStatusCode();

        if (status == HttpStatus.UNAUTHORIZED) {
            log.error("❌ [KAKAO][GEO] 인증 실패 {}={} status={} msg={}",
                    keyName, keyValue, status, e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_API_UNAUTHORIZED);
        }
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            log.error("❌ [KAKAO][GEO] 호출 한도 초과 {}={} status={} msg={}",
                    keyName, keyValue, status, e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_API_RATE_LIMIT);
        }

        log.error("❌ [KAKAO][GEO] HTTP 오류 {}={} status={} msg={}",
                keyName, keyValue, status, e.getMessage());
    }
}