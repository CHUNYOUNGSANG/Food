package project.food.domain.restaurant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.restaurant.entity.Restaurant;
import project.food.domain.restaurant.repository.RestaurantRepository;
import project.food.global.api.kakao.local.dto.KakaoKeywordResponse;
import project.food.global.api.kakao.local.service.KakaoLocalService;

/**
 * 맛집 동기화 서비스
 * - 카카오 키워드 검색 API를 통해 맛집 정보를 수집하여 DB에 저장
 * - 중복 저장 방지를 위해 카카오 place id(sourceId)로 체크
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RestaurantSyncService {

    private final KakaoLocalService kakaoLocalService;
    private final RestaurantRepository restaurantRepository;

    /**
     * 카카오 키워드 검색으로 맛집 정보 수집 및 저장
     *
     * @param keyword 검색 키워드 (ex: "강남맛집", "홍대 파스타")
     * @param page    카카오 API 페이지 번호 (1부터 시작)
     * @return 새로 저장된 맛집 수
     */
    public int syncByKeyword(String keyword, int page) {
        log.info("[SYNC] 카카오 맛집 수집 시작 keyword={} page={}", keyword, page);

        KakaoKeywordResponse response = kakaoLocalService.searchPlaceByKeyword(keyword, page);

        int savedCount = 0;

        for (KakaoKeywordResponse.Place doc : response.getDocuments()) {
            // 카카오 place id를 sourceId로 사용 (고유한 장소 식별자)
            String sourceId = doc.getId();

            // 이미 저장된 맛집이면 건너뜀
            if (restaurantRepository.existsBySourceId(sourceId)) {
                continue;
            }

            // 도로명 주소 우선, 없으면 지번 주소 사용
            String address = (doc.getRoadAddressName() != null && !doc.getRoadAddressName().isBlank())
                    ? doc.getRoadAddressName()
                    : doc.getAddressName();

            Restaurant restaurant = Restaurant.builder()
                    .sourceId(sourceId)
                    .name(doc.getPlaceName())
                    .address(address)
                    .category(doc.getCategoryName())
                    .latitude(doc.getY() != null ? Double.parseDouble(doc.getY()) : null)
                    .longitude(doc.getX() != null ? Double.parseDouble(doc.getX()) : null)
                    .placeUrl(doc.getPlaceUrl())
                    .build();

            restaurantRepository.save(restaurant);
            savedCount++;
        }

        log.info("[SYNC] 저장 완료 savedCount={}", savedCount);
        return savedCount;
    }
}