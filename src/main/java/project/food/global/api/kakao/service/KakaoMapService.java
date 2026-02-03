package project.food.global.api.kakao.service;

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
import project.food.global.api.kakao.dto.KakaoAddressResponse;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;


/**
 * 카카오 지도 API 서비스
 * 주소 -> 좌표
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoMapService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private static final String KAKAO_ADDRESS_API_URL =
            "https://dapi.kakao.com/v2/local/search/address.json";

    private final RestTemplate restTemplate;

    /**
     * 주소로 좌표 조회
     * @param address 검색할 주소
     * @return 좌표 정보
     */
    public KakaoAddressResponse getCoordinateByAddress(String address) {
        log.debug("카카오 주소 검색 시작: address={}", address);

        // URI 생성
        String uri = UriComponentsBuilder
                .fromHttpUrl(KAKAO_ADDRESS_API_URL)
                .queryParam("query", address)
                .build()
                .toUriString();

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoAddressResponse> response =
                    restTemplate.exchange(
                            uri,
                            HttpMethod.GET,
                            entity,
                            KakaoAddressResponse.class
                    );

            KakaoAddressResponse body = response.getBody();

            if (body == null || !body.hasResult()) {
                log.warn("카카오 주소 검색 결과 없음: address={}", address);
                throw new CustomException(ErrorCode.KAKAO_ADDRESS_NOT_FOUND);
            }

            log.info("✅ 카카오 주소 검색 성공: address={}, lat={}, lng={}",
                    address, body.getLatitude(), body.getLongitude());

            return body;

        } catch (CustomException e) {
            throw e;
        } catch (HttpClientErrorException e) {
            handleHttpError(e, address);
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        } catch (RestClientException e) {
            log.error("⚠️카카오 API 호출 중 오류: address={}, error={}",
                    address, e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    /**
     * HTTP 에러 처리
     */
    private void handleHttpError(HttpClientErrorException e, String
            address) {
        HttpStatusCode status = e.getStatusCode();

        if (status == HttpStatus.UNAUTHORIZED) {
            log.error("❌ 카카오 API 인증 실패: address={}", address);
            throw new CustomException(ErrorCode.KAKAO_API_UNAUTHORIZED);
        }
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            log.error("❌ 카카오 API 호출 한도 초과: address={}",
                    address);
            throw new CustomException(ErrorCode.KAKAO_API_RATE_LIMIT);
        }

        log.error("❌ 카카오 API 호출 실패: address={}, status={}, error={}",
                address, status, e.getMessage());
    }
}
