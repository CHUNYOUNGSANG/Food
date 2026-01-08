package project.food.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.food.global.enums.Role;


@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
    @SequenceGenerator(name = "member_seq", sequenceName = "MEMBER_SEQ", initialValue = 1, allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String nickname;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 700)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Builder
    public Member(String email, String password, String name,
                  String nickname, String profileImage) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = Role.USER;
    }

    // 회원 정보 수정 메서드
    public void updateProfile(String name, String nickname, String profileImage) {
        this.name = name;
        this.nickname = nickname;
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    // 비밀번호 변경 메서드
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    // 권한 확인 메서드
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

}
