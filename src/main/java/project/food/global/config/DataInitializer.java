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
import project.food.domain.restaurant.entity.Restaurant;
import project.food.domain.restaurant.repository.RestaurantRepository;
import project.food.global.api.kakao.local.dto.KakaoKeywordResponse;
import project.food.global.api.kakao.local.service.KakaoLocalService;
import project.food.global.api.naver.NaverSearchService;
import project.food.global.enums.Role;
import project.food.global.file.service.FileStorage;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestaurantRepository restaurantRepository;
    private final KakaoLocalService kakaoLocalService;
    private final NaverSearchService naverSearchService;
    private final FileStorage fileStorage;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    // 초기 맛집 검색 키워드 목록
    private static final List<String> INIT_KEYWORDS = List.of(
            "강남 맛집", "홍대 맛집", "이태원 맛집", "신촌 맛집", "건대 맛집"
    );

    @Bean
    @Profile({"dev", "prod"})
    public CommandLineRunner initData() {
        return args -> {
            log.info("============================");
            log.info("===== 초기 데이터 생성 시작 =====");
            log.info("============================");

            createAdminIfNotExists();
            initRestaurantsIfEmpty();

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
        log.info(" - 권한: ADMIN");

    }

    /**
     * 맛집 초기 데이터 생성
     * DB가 비어있을 때만 실행 → 서버 재시작마다 카카오/네이버 API 호출 방지
     */
    private void initRestaurantsIfEmpty() {
        if (restaurantRepository.count() > 0) {
            log.info("맛집 데이터가 이미 존재합니다. 초기화 스킵");
            return;
        }

        log.info("맛집 초기 데이터 생성 시작: keywords={}", INIT_KEYWORDS);
        int totalSaved = 0;

        for (String keyword : INIT_KEYWORDS) {
            try {
                totalSaved += saveRestaurantsByKeyword(keyword);
            } catch (Exception e) {
                // 키워드 하나 실패해도 나머지 계속 진행
                log.warn("키워드 초기화 실패 (스킵): keyword={}, error={}", keyword, e.getMessage());
            }
        }

        log.info("맛집 초기 데이터 생성 완료: 총 {}개 저장", totalSaved);
    }

    /**
     * 키워드로 카카오 검색 → 맛집 저장 → 네이버로 이미지 저장
     */
    private int saveRestaurantsByKeyword(String keyword) {
        log.debug("키워드 맛집 검색 시작: keyword={}", keyword);

        KakaoKeywordResponse response = kakaoLocalService.searchPlaceByKeyword(keyword, 1);
        if (!response.hasResult()) {
            log.warn("카카오 검색 결과 없음: keyword={}", keyword);
            return 0;
        }

        int savedCount = 0;

        for (KakaoKeywordResponse.Place place : response.getDocuments()) {
            try {
                // 이미 저장된 맛집이면 스킵 (sourceId로 중복 체크)
                if (restaurantRepository.existsBySourceId(place.getId())) {
                    log.debug("이미 존재하는 맛집 스킵: sourceId={}", place.getId());
                    continue;
                }

                // 주소는 도로명 우선, 없으면 지번 사용
                String address = place.getRoadAddressName() != null && !place.getRoadAddressName().isEmpty()
                        ? place.getRoadAddressName()
                        : place.getAddressName();

                Restaurant restaurant = Restaurant.builder()
                        .sourceId(place.getId())
                        .name(place.getPlaceName())
                        .address(address)
                        .category(place.getCategoryName())
                        .latitude(parseDouble(place.getY()))
                        .longitude(parseDouble(place.getX()))
                        .placeUrl(place.getPlaceUrl())
                        .build();

                Restaurant saved = restaurantRepository.save(restaurant);

                // 네이버로 이미지 검색 → S3 저장
                saveRestaurantImage(saved, place.getPlaceName());

                savedCount++;
                log.debug("맛집 저장: name={}, sourceId={}", place.getPlaceName(), place.getId());

            } catch (Exception e) {
                log.warn("맛집 저장 실패 (스킵): name={}, error={}", place.getPlaceName(), e.getMessage());
            }
        }

        log.info("키워드 맛집 저장 완료: keyword={}, savedCount={}", keyword, savedCount);
        return savedCount;
    }

    /**
     * 네이버 이미지 검색 → 다운로드 → S3 저장 → Restaurant.imageUrl 업데이트
     * 실패해도 맛집 저장 자체는 유지됨
     */
    private void saveRestaurantImage(Restaurant restaurant, String restaurantName) {
        try {
            byte[] imageBytes = naverSearchService.searchRestaurantImage(restaurantName);
            if (imageBytes == null) return;

            String s3Url = fileStorage.saveRestaurantImage(imageBytes, restaurantName + ".jpg");
            restaurant.updateImageUrl(s3Url);
            restaurantRepository.save(restaurant);

            log.debug("맛집 이미지 저장 완료: name={}, url={}", restaurantName, s3Url);
        } catch (Exception e) {
            log.warn("맛집 이미지 저장 실패 (스킵): name={}, error={}", restaurantName, e.getMessage());
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}