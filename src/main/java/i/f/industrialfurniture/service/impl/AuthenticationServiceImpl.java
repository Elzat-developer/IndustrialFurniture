package i.f.industrialfurniture.service.impl;

import i.f.industrialfurniture.dto.auth.JwtAuthenticationResponce;
import i.f.industrialfurniture.dto.auth.SignInRequest;
import i.f.industrialfurniture.model.entity.User;
import i.f.industrialfurniture.repositories.UserRepo;
import i.f.industrialfurniture.service.AuthenticationService;
import i.f.industrialfurniture.service.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    @Override
    public JwtAuthenticationResponce signIn(SignInRequest signInRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.email(),
                        signInRequest.password()
                )
        );
        User user = userRepo.findByEmail(signInRequest.email());
        var jwt = jwtService.generateToken(user);

        return new JwtAuthenticationResponce(jwt);
    }
}
