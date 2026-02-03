package i.f.industrialfurniture.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос для авторизации пользователя")
public record SignInRequest(

        @Schema(description = "Электронная почта", example = "user@gmail.com")
        @NotBlank(message = "Email is required")
        String email,

        @Schema(description = "Пароль", example = "123456")
        @NotBlank(message = "Password is required")
        String password
) {}
