package project.food.global.api.kakao.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import project.food.global.api.kakao.oauth.dto.KakaoTokenResponse;
import project.food.global.api.kakao.oauth.dto.KakaoUserInfoResponse;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthService {

    @Value("${kakao.api.key}")
    private String clientId;

    @Value("${kakao.oauth.redirect-uri}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestTemplate restTemplate;

    /**
     * 인가코드 → 카카오 액세스 토큰 교환
     */
    public KakaoTokenResponse getToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                    TOKEN_URL, HttpMethod.POST, request, KakaoTokenResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("[KAKAO][OAUTH] 토큰 교환 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    /**
     * 카카오 액세스 토큰 → 사용자 정보 조회
     */
    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                    USER_INFO_URL, HttpMethod.GET, request, KakaoUserInfoResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("[KAKAO][OAUTH] 사용자 정보 조회 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }
    }
}