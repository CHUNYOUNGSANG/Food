package project.food.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.food.domain.member.entity.Member;
import project.food.domain.member.repository.MemberRepository;
import project.food.global.enums.Role;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    @Profile({"dev", "local"})
    public CommandLineRunner initData() {
        return args -> {
            log.info("============================");
            log.info("===== 초기 데이터 생성 시작 =====");
            log.info("============================");

            // 관리자 계정 생성
            createAdminIfNotExists();

            log.info("============================");
            log.info("===== 초기 데이터 생성 완료 =====");
            log.info("============================");
        };
    }

    /**
     * 관리자 계정 생성
     * 이미 존재하는 경우 생성하지 않음
     */
    private void createAdminIfNotExists() {

        if (memberRepository.existsByEmail(adminEmail)) {
            log.info("관리자 계정이 이미 존재합니다: {}", adminEmail);
            return;
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(adminPassword);

        Member admin = Member.builder()
                .email(adminEmail)
                .password(encodedPassword)
                .name("관리자")
                .nickname("Admin")
                .build();
        admin.promoteToAdmin();
        memberRepository.save(admin);

        log.info("관리자 계정 생성 완료");
        log.info(" - 이메일: {}", adminEmail);
        log.info(" - 암호화된 비밀번호: {}", encodedPassword);
        log.info(" - 권한: ADMIN");
    }
}
