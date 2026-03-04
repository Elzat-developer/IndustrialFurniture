package i.f.industrialfurniture.dto.auth;


public record SignInRequest(
        String email,
        String password
) {}
