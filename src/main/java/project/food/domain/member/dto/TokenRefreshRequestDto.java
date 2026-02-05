package project.food.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenRefreshRequestDto {

    @NotBlank(message = "리프세쉬 토큰은 필수입니다.")
    private String refreshToken;
}
