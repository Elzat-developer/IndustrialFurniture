package i.f.industrialfurniture.controller;

import i.f.industrialfurniture.dto.auth.JwtAuthenticationResponce;
import i.f.industrialfurniture.dto.auth.SignInRequest;
import i.f.industrialfurniture.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Эндпоинты для регистрации и входа пользователя")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/sign-in")
    @Operation(
            summary = "Авторизация пользователя",
            description = "Принимает телефон и пароль, возвращает JWT токен"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешная авторизация",
            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponce.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Неверный логин или пароль"
    )
    public ResponseEntity<JwtAuthenticationResponce> signIn(
            @RequestBody SignInRequest signInRequest
    ) {
        return new ResponseEntity<>(authenticationService.signIn(signInRequest), HttpStatus.OK);
    }
}
