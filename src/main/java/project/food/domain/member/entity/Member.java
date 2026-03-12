package project.food.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import project.food.global.common.BaseTimeEntity;
import project.food.global.enums.Role;

import java.time.LocalDateTime;


@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String nickname;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Lob
    @Column(name = "profile_image", columnDefinition = "LONGTEXT")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(nullable = false)
    private LocalDateTime joinDate;

    @Builder
    public Member(String email, String password, String name,
                  String nickname, String profileImage) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = Role.USER;
        this.joinDate = LocalDateTime.now();
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

    // 관리자 권한 설정 메서드
    public void promoteToAdmin() {
        this.role = Role.ADMIN;
    }

}
