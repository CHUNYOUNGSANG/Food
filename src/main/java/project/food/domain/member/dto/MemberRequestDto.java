package project.food.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.food.domain.member.entity.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRequestDto {

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다.")
    private String nickname;

    private String profileImage;

    @Builder
    public MemberRequestDto(String email, String password, String name,
                            String nickname, String profileImage) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .email(this.email)
                .password(encodedPassword)
                .name(this.name)
                .nickname(this.nickname)
                .profileImage(this.profileImage)
                .build();
    }
}
