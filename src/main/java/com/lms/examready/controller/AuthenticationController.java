package com.lms.examready.controller;

import com.lms.examready.dto.request.SignInRequestDto;
import com.lms.examready.dto.request.SignUpRequestDto;
import com.lms.examready.dto.response.UserResponseDto;
import com.lms.examready.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-up")
    public Mono<ResponseEntity<UserResponseDto>> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {

        return authenticationService.signUp(signUpRequestDto)
                .map(userResponseDto -> new ResponseEntity<>(userResponseDto, CREATED));
    }

    @PostMapping("/sign-in")
    public Mono<ResponseEntity<Void>> signIn(@RequestBody SignInRequestDto signInRequestDto) {
        return authenticationService.signIn(signInRequestDto)
                .map(jwt -> {
                            HttpHeaders headers = new HttpHeaders();
                            headers.add(AUTHORIZATION, "Bearer " + jwt);
                            return new ResponseEntity<Void>(headers, OK);
                        }
                )
                .onErrorResume(BadCredentialsException.class, e ->
                        Mono.just(new ResponseEntity<>(UNAUTHORIZED)));
    }
}
