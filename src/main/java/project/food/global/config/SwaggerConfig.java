package project.food.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT Bearer 인증 스키마 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT 토큰을 입력하세요 (Bearer 접두사 불필요)");

        return new OpenAPI()
                .info(new Info()
                        .title("맛집 API")
                        .description("맛집 리뷰 API 문서")
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server()
                                .url("http://api.fineeat.kro.kr:8080")
                                .description("운영 서버"),
                        new Server()
                                .url("http://52.78.34.150:8080")
                                .description("EC2 서버")
                ))
                // Authorize 버튼에 JWT 인증 추가
                .components(new Components()
                        .addSecuritySchemes("Bearer Token", securityScheme))
                // 모든 API에 기본으로 인증 적용 (permitAll API는 토큰 없이도 동작)
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"));
    }
}