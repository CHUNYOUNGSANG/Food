package project.food.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.member.dto.LoginResponseDto;
import project.food.domain.member.dto.MemberResponseDto;
import project.food.domain.member.entity.Member;
import project.food.domain.member.repository.MemberRepository;
import project.food.global.api.kakao.oauth.dto.KakaoTokenResponse;
import project.food.global.api.kakao.oauth.dto.KakaoUserInfoResponse;
import project.food.global.api.kakao.oauth.service.KakaoOAuthService;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;
import project.food.global.jwt.JwtTokenProvider;
import project.food.global.jwt.RefreshTokenService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OAuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * 카카오 OAuth 로그인
     * 1. 인가코드 → 카카오 액세스 토큰
     * 2. 카카오 액세스 토큰 → 사용자 정보 (이메일, 닉네임)
     * 3. DB에 회원 없으면 자동 가입, 있으면 로그인
     * 4. JWT 발급
     */
    public LoginResponseDto kakaoLogin(String code) {
        // 1. 인가코드로 카카오 액세스 토큰 발급
        KakaoTokenResponse tokenResponse = kakaoOAuthService.getToken(code);

        // 2. 카카오 액세스 토큰으로 사용자 정보 조회
        KakaoUserInfoResponse userInfo = kakaoOAuthService.getUserInfo(tokenResponse.getAccessToken());

        String email = userInfo.getEmail();
        if (email == null) {
            log.error("[KAKAO][OAUTH] 이메일 정보 없음: kakaoId={}", userInfo.getId());
            throw new CustomException(ErrorCode.KAKAO_API_ERROR);
        }

        // 3. 이메일로 회원 조회 → 없으면 자동 가입
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> registerOAuthMember(email, userInfo.getNickname()));

        // 4. JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(
                member.getId(), member.getEmail(), member.getRole().getKey());
        String refreshToken = jwtTokenProvider.createRefreshToken(
                member.getId(), member.getRole().getKey());

        refreshTokenService.save(member.getId(), refreshToken);

        log.info("[KAKAO][OAUTH] 로그인 완료: memberId={}, email={}", member.getId(), email);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(MemberResponseDto.from(member))
                .build();
    }

    /**
     * OAuth 신규 회원 자동 가입
     * - 비밀번호: UUID (로그인 불가, OAuth 전용)
     * - 닉네임 중복 시 숫자 접미사 추가
     */
    private Member registerOAuthMember(String email, String kakaoNickname) {
        String nickname = resolveUniqueNickname(kakaoNickname != null ? kakaoNickname : "카카오유저");
        String dummyPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        Member member = Member.builder()
                .email(email)
                .password(dummyPassword)
                .name(nickname)
                .nickname(nickname)
                .build();

        Member saved = memberRepository.save(member);
        log.info("[KAKAO][OAUTH] 신규 회원 가입: memberId={}, email={}", saved.getId(), email);
        return saved;
    }

    private String resolveUniqueNickname(String base) {
        String nickname = base;
        int suffix = 1;
        while (memberRepository.existsByNickname(nickname)) {
            nickname = base + suffix++;
        }
        return nickname;
    }
}