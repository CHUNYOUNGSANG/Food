package project.food.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import project.food.domain.member.entity.Member;
import project.food.global.enums.Role;

@Getter
@Builder
public class MemberResponseDto {

    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String profileImage;
    private Role role;

    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .role(member.getRole())
                .build();
    }
}
