package project.food.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.food.domain.member.dto.LoginResponseDto;
import project.food.domain.member.service.OAuthService;

@Tag(name = "OAuth", description = "소셜 로그인 API")
@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oAuthService;

    @Operation(summary = "카카오 로그인", description = "카카오 인가코드로 로그인합니다.")
    @GetMapping("/kakao")
    public ResponseEntity<LoginResponseDto> kakaoLogin(@RequestParam String code) {
        LoginResponseDto response = oAuthService.kakaoLogin(code);
        return ResponseEntity.ok(response);
    }
}