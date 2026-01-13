package project.food.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import project.food.domain.member.entity.Member;
import project.food.global.enums.Role;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberResponseDto {

    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String profileImage;
    private Role role;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .role(member.getRole())
                .joinDate(member.getJoinDate())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
