package project.food.domain.restaurant.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.food.domain.restaurant.service.RestaurantSyncService;

import java.util.Map;

/**
 * 맛집 관리자 API 컨트롤러
 * - 카카오 API를 통한 맛집 정보 수집 트리거
 */
@RestController
@RequestMapping("/admin/restaurants")
@RequiredArgsConstructor
@Slf4j
public class RestaurantAdminController {

    private final RestaurantSyncService restaurantSyncService;

    /**
     * 카카오 키워드 기반 맛집 수집
     * - 키워드로 카카오 장소 검색 후 DB에 저장
     *
     * ex) GET /admin/restaurants/sync?keyword=강남맛집&page=1
     *
     * @param keyword 검색 키워드
     * @param page    카카오 API 페이지 번호 (1부터 시작)
     * @return 검색 키워드, 페이지, 저장된 맛집 수
     */
    @GetMapping("/sync")
    public Map<String, Object> sync(
            @RequestParam(defaultValue = "강남맛집") String keyword,
            @RequestParam(defaultValue = "1") int page
    ) {
        int savedCount = restaurantSyncService.syncByKeyword(keyword, page);

        return Map.of(
                "keyword", keyword,
                "page", page,
                "savedCount", savedCount
        );
    }
}