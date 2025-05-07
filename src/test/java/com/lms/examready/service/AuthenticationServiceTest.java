package com.lms.examready.service;

import com.lms.examready.dto.request.SignInRequestDto;
import com.lms.examready.dto.request.SignUpRequestDto;
import com.lms.examready.dto.response.UserResponseDto;
import com.lms.examready.exception.UserAlreadyExistsException;
import com.lms.examready.model.User;
import com.lms.examready.security.jwt.JwtProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class AuthenticationServiceTest {


    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AutoCloseable mockitoSession;

    @BeforeEach
    void setUp() {
        mockitoSession = openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoSession.close();
    }

    @Test
    void signUp_UserAlreadyExists_ThrowsException() {
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("existingUser", "password", "email@example.com");
        when(userService.findByUsername(signUpRequestDto.username()))
                .thenReturn(Mono.just(new User()));

        Mono<UserResponseDto> result = authenticationService.signUp(signUpRequestDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof UserAlreadyExistsException &&
                        throwable.getMessage().equals("Username 'existingUser' already exists"))
                .verify();
        verify(userService, times(1)).findByUsername(signUpRequestDto.username());
        verify(userService, never()).saveUser(any());
    }

    @Test
    void signUp_NewUser_SavesSuccessfully() {
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("newUser", "password", "email@example.com");
        User savedUser = new User();
        when(userService.findByUsername(signUpRequestDto.username()))
                .thenReturn(Mono.empty());
        when(userService.saveUser(signUpRequestDto))
                .thenReturn(Mono.just(savedUser));

        Mono<UserResponseDto> result = authenticationService.signUp(signUpRequestDto);

        StepVerifier.create(result)
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();
        verify(userService, times(1)).findByUsername(signUpRequestDto.username());
        verify(userService, times(1)).saveUser(signUpRequestDto);
    }

    @Test
    void signIn_InvalidUsername_ThrowsException() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("invalidUser", "password");
        when(userService.findByUsername(signInRequestDto.username()))
                .thenReturn(Mono.empty());

        Mono<String> result = authenticationService.signIn(signInRequestDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BadCredentialsException &&
                        throwable.getMessage().equals("Invalid username or password"))
                .verify();
        verify(userService, times(1)).findByUsername(signInRequestDto.username());
    }

    @Test
    void signIn_DisabledAccount_ThrowsException() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("disabledUser", "password");
        User disabledUser = new User();
        disabledUser.setEnabled(false);
        when(userService.findByUsername(signInRequestDto.username()))
                .thenReturn(Mono.just(disabledUser));

        Mono<String> result = authenticationService.signIn(signInRequestDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BadCredentialsException &&
                        throwable.getMessage().equals("Account is disable"))
                .verify();
        verify(userService, times(1)).findByUsername(signInRequestDto.username());
    }

    @Test
    void signIn_ValidCredentials_ReturnsJwt() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("validUser", "password");
        User validUser = new User();
        validUser.setEnabled(true);
        validUser.setPassword(passwordEncoder.encode("password"));
        when(userService.findByUsername(signInRequestDto.username()))
                .thenReturn(Mono.just(validUser));
        when(passwordEncoder.matches(signInRequestDto.password(), validUser.getPassword()))
                .thenReturn(true);
        when(jwtProvider.generateToken(validUser.getId(), validUser.getUsername(), validUser.getRole()))
                .thenReturn("test-jwt");

        Mono<String> result = authenticationService.signIn(signInRequestDto);

        StepVerifier.create(result)
                .expectNext("test-jwt")
                .verifyComplete();
        verify(userService, times(1)).findByUsername(signInRequestDto.username());
        verify(jwtProvider, times(1)).generateToken(validUser.getId(), validUser.getUsername(), validUser.getRole());
    }

    @Test
    void signIn_InvalidCredentials_ThrowsBadCredentialsException() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("validUser", "password");
        User validUser = new User();
        validUser.setEnabled(true);
        validUser.setPassword(passwordEncoder.encode("password"));
        when(userService.findByUsername(signInRequestDto.username()))
                .thenReturn(Mono.just(validUser));
        when(passwordEncoder.matches(signInRequestDto.password(), validUser.getPassword()))
                .thenReturn(false);

        Mono<String> result = authenticationService.signIn(signInRequestDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BadCredentialsException &&
                        throwable.getMessage().equals("Invalid username or password"))
                .verify();
        verify(userService, times(1)).findByUsername(signInRequestDto.username());
        verify(jwtProvider, times(0)).generateToken(validUser.getId(), validUser.getUsername(), validUser.getRole());
    }
}
