package project.food.domain.tag.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.food.domain.tag.dto.TagResponseDto;
import project.food.domain.tag.dto.TagUpdateDto;
import project.food.domain.tag.service.TagService;

import java.util.List;

@Tag(name = "Tag", description = "태그 API")
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;

    /**
     * 게시글 생성
     * POST /api/tags
     *
     * @param request 태그 생성 요청 데이터
     * @return 생성된 태그 (201 Created)
     */
    @Operation(summary = "태그 생성", description = "새로운 태그를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "태그 생성 성공",
                    content = @Content(schema = @Schema(implementation = TagResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "409", description = "중복된 태그 이름")
    })
    @PostMapping
    public ResponseEntity<TagResponseDto> createTag(
            @Valid @RequestBody TagUpdateDto request) {

        log.info("태그 생성 요청: name={}", request.getName());

        TagResponseDto response = tagService.createTag(request);

        log.info("태그 생성 완료: tagId={}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    /**
     * 태그 전체 조회
     * * GET /api/tags
     * @return 태그 전체 목록 조회 (200 OK)
     */
    @Operation(summary = "태그 전체 목록 조회", description = "모든 태그를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<TagResponseDto>> getAllTags() {

        log.info("태그 전체 목록 조회 요청");

        List<TagResponseDto> tags = tagService.getAllTags();

        log.info("태그 전체 목록 조회 완료: tagCount={}", tags.size());

        return ResponseEntity.ok(tags);
    }

    /**
     * 태그 단건 조회
     * GET /api/tags
     * @param tagId 태그 ID
     * @return 조회된 태그 ID (200 OK)
     */
    @Operation(summary = "태그 단건 조회", description = "특정 태그를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음")
    })
    @GetMapping("/{tagId}")
    public ResponseEntity<TagResponseDto> getTag(
            @Parameter(description = "태그 ID", required = true)
            @PathVariable Long tagId) {

        log.info("태그 조회 요청: tagId={}", tagId);

        TagResponseDto response =
                tagService.getTag(tagId);

        log.info("태그 조회 완료: tagId={}, name={}",
                tagId, response.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * 태그 수정
     * PUT /api/tags/{tagId}
     * @param tagId 태그 ID
     * @param request 수정 요청 데이터
     * @return 수정된 태그 정보 (200 OK)
     */
    @Operation(summary = "태그 수정", description = "태그 이름을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "중복된 태그 이름")
    })
    @PutMapping("/{tagId}")
    public ResponseEntity<TagResponseDto> updateTag(
            @Parameter(description = "태그 ID", required = true)
            @PathVariable Long tagId,
            @Valid @RequestBody TagUpdateDto request) {

        log.info("태그 수정 요청: tagId={}, newName={}", tagId, request.getName());

        TagResponseDto response = tagService.updateTag(tagId, request);

        log.info("태그 수정 완료: tagId={}", tagId);

        return ResponseEntity.ok(response);

    }

    /**
     * 태그 삭제
     * DELETE /api/tags/{tagId}
     * @param tagId 태그 ID
     * @return 204 No Content
     */
    @Operation(summary = "태그 삭제", description = "태그를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "태그를 찾을 수 없음")
    })
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(
            @Parameter(description = "태그 ID", required = true)
            @PathVariable Long tagId) {

        log.info("태그 삭제 요청: tagId={}", tagId);

        tagService.deleteTag(tagId);

        log.info("태그 삭제 완료: tagId={}", tagId);

        return ResponseEntity.noContent().build();

    }

    /**
     * 태그 검색
     * GET /api/tags/search?keyword=맛집
     * @param keyword 검색 키워드
     * @return 검색 결과 태그 목록 (200 OK)
     */
    @Operation(summary = "태그 검색", description = "이름에 키워드가 포함된 태그를 검색합니다.")
    @ApiResponse(responseCode = "200", description = "검색 성공")
    @GetMapping("/search")
    public ResponseEntity<List<TagResponseDto>>
    searchTags(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword) {

        log.info("태그 검색 요청: keyword={}", keyword);

        List<TagResponseDto> tags = tagService.searchTags(keyword);

        log.info("태그 검색 완료: keyword={}, resultCount={}", keyword, tags.size());

        return ResponseEntity.ok(tags);

    }
}

