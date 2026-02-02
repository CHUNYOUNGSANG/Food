package project.food.domain.tag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.food.domain.tag.dto.TagResponseDto;
import project.food.domain.tag.dto.TagUpdateDto;
import project.food.domain.tag.entity.Tag;
import project.food.domain.tag.repository.TagRepository;
import project.food.global.exception.CustomException;
import project.food.global.exception.ErrorCode;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TagService {

    private final TagRepository tagRepository;

    /**
     * 태그 생성
     */
    @Transactional
    public TagResponseDto createTag(TagUpdateDto request) {
        log.debug("태그 생성 시작: name={}", request.getName());

        if (tagRepository.existsByName(request.getName())) {
            log.warn("⚠️태그 이름 중복: name={}", request.getName());
            throw new CustomException(ErrorCode.DUPLICATE_TAG_NAME);
        }

        Tag tag = Tag.builder()
                .name((request.getName()))
                .build();

        Tag savedTag = tagRepository.save(tag);

        log.info("✅태그 생성 완료: tagId={}, name={}", savedTag.getId(), request.getName());

        return TagResponseDto.from(savedTag);
    }

    /**
     * 태그 전체 목록 조회
     */
    public List<TagResponseDto> getAllTags() {
        log.debug("태그 전체 목록 조회 시작");

        List<Tag> tags = tagRepository.findAll();

        log.info("✅태그 전체 목록 조회 완료: totalCount={}", tags.size());

        return tags.stream()
                .map(TagResponseDto::from)
                .collect(Collectors.toList());

    }

    /**
     * 태그 조회
     */
    public TagResponseDto getTag(Long tagId) {
        log.debug("태그 조회 시작: tagId={}", tagId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> {
                    log.error("❌태그 찾기 실패: tagId={}", tagId);
                    return new CustomException(ErrorCode.TAG_NOT_FOUND);
                });

        log.info("✅태그 조회 완료: tagId={}, name={}", tag.getId(), tag.getName());

        return TagResponseDto.from(tag);
    }

    /**
     * 태그 수정
     */
    @Transactional
    public TagResponseDto updateTag(Long tagId, TagUpdateDto request) {

        log.debug("태그 수정 시작: tagId={}, newName={}", tagId, request.getName());

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> {
                    log.error("❌태그 찾기 실패: tagId={}", tagId);
                    return new CustomException(ErrorCode.TAG_NOT_FOUND);
                });

        if (tagRepository.existsByName(request.getName()) && !tag.getName().equals(request.getName())) {
            log.warn("⚠️태그 이름 중복: name={}", request.getName());

            throw new CustomException(ErrorCode.DUPLICATE_TAG_NAME);
        }

        String oldName = tag.getName();
        tag.updateName(request.getName());

        log.info("✅태그 수정 완료: tagId={}, {} -> {}", tagId, oldName, tag.getName());

        return TagResponseDto.from(tag);
    }

    /**
     * 태그 삭제
     */
    @Transactional
    public void deleteTag(Long tagId) {
        log.debug("태그 삭제 시작: tagId={}", tagId);

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> {
                    log.error("❌태그 찾기 실패: tagId={}", tagId);
                    return new CustomException(ErrorCode.TAG_NOT_FOUND);
                });

        tagRepository.delete(tag);

        log.info("✅태그 삭제 완료: tagId={}, name={}", tagId, tag.getName());
    }

    /**
     * 태그 검색
     */
    public List<TagResponseDto> searchTags(String keyword) {
        log.debug("태그 검색 시작: keyword={}", keyword);

        List<Tag> tags = tagRepository.findByNameContaining(keyword);

        log.info("✅태그 검색 완료: keyword={}, resultCount={}", keyword, tags.size());

        return tags.stream()
                .map(TagResponseDto::from)
                .collect(Collectors.toList());

    }
}
