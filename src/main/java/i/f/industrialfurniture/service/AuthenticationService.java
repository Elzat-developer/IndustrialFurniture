package i.f.industrialfurniture.service;

import i.f.industrialfurniture.dto.auth.JwtAuthenticationResponce;
import i.f.industrialfurniture.dto.auth.SignInRequest;

public interface AuthenticationService {
    JwtAuthenticationResponce signIn(SignInRequest signInRequest);
}
