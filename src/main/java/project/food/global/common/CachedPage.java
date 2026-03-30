package project.food.global.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * Redis 캐싱 가능한 Page 래퍼
 * PageImpl은 Jackson 역직렬화 불가 → 이 클래스로 대체
 */
public class CachedPage<T> extends PageImpl<T> {

    @JsonCreator
    public CachedPage(
            @JsonProperty("content") List<T> content,
            @JsonProperty("number") int page,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements) {
        super(content, PageRequest.of(page, size), totalElements);
    }
}