package com.lms.examready.service;

import com.lms.examready.dto.request.SignInRequestDto;
import com.lms.examready.dto.request.SignUpRequestDto;
import com.lms.examready.dto.response.UserResponseDto;
import com.lms.examready.exception.UserAlreadyExistsException;
import com.lms.examready.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public Mono<UserResponseDto> signUp(SignUpRequestDto signUpRequestDto) {
        return userService.findByUsername(signUpRequestDto.username())
                .flatMap(existingUser -> Mono.<UserResponseDto>error(
                        new UserAlreadyExistsException("Username '" + signUpRequestDto.username() + "' already exists")
                ))
                .switchIfEmpty(
                        Mono.defer(() -> userService.saveUser(signUpRequestDto).map(UserResponseDto::from))
                );
    }

    public Mono<String> signIn(SignInRequestDto signInRequestDto) {
        return userService.findByUsername(signInRequestDto.username())
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password")))
                .flatMap(user -> {
                    if (!user.isEnabled()) {
                        return Mono.error(new BadCredentialsException("Account is disabled"));
                    } else if (passwordEncoder.matches(signInRequestDto.password(), user.getPassword())) {
                        return Mono.just(jwtProvider.generateToken(user.getId(), user.getUsername(), user.getRole()));
                    } else {
                        return Mono.error(new BadCredentialsException("Invalid username or password"));
                    }
                });
    }

}
