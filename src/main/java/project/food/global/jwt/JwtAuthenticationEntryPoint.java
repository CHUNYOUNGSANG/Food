package project.food.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import project.food.global.exception.ErrorCode;
import project.food.global.exception.ErrorResponse;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode);

        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
