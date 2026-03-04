package i.f.industrialfurniture.controller;

import i.f.industrialfurniture.dto.auth.JwtAuthenticationResponce;
import i.f.industrialfurniture.dto.auth.SignInRequest;
import i.f.industrialfurniture.service.AuthenticationService;
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
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    @PostMapping("/sign-in")
    public ResponseEntity<JwtAuthenticationResponce> signIn(
            @RequestBody SignInRequest signInRequest
    ) {
        return new ResponseEntity<>(authenticationService.signIn(signInRequest), HttpStatus.OK);
    }
}
