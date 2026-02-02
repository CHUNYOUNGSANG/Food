package project.food.domain.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Schema(description = "태그 생성/수정 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagUpdateDto {

    @Schema(description = "태그 이름", example = "남친이랑")
    @NotBlank(message = "태그 이름은 필수 입니다.")
    @Size(max = 50, message = "태그 이름은 50자 이내로 입력해주세요.")
    private String name;
}
