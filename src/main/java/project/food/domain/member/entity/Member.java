package project.food.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
    @SequenceGenerator(name = "member_seq", sequenceName = "MEMBER_SEQ", initialValue = 1, allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "nickname", nullable = false, length = 50, unique = true)
    private String nickname;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "profile_image", length = 700)
    private String profileImage;

    @Builder
    public Member(String email, String password, String name,
                  String nickname, String profileImage) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    // 회원 정보 수정
    public void updateProfile(String name, String nickname, String profileImage) {
        this.name = name;
        this.nickname = nickname;
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

}
