package com.lms.examready.controller;

import com.lms.examready.dto.request.SignInRequestDto;
import com.lms.examready.dto.request.SignUpRequestDto;
import com.lms.examready.dto.response.UserResponseDto;
import com.lms.examready.service.AuthenticationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static com.lms.examready.model.Role.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    private AuthenticationController authenticationController;

    private AutoCloseable mockitoSession;

    @BeforeEach
    void setUp() {
        mockitoSession = openMocks(this);
        authenticationController = new AuthenticationController(authenticationService);
    }

    @AfterEach
    void tearDown() throws Exception{
        mockitoSession.close();
    }

    @Test
    void testSignUp_Success() {
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("testUser", "testPassword", "testEmail@example.com");
        UserResponseDto userResponseDto = new UserResponseDto("testUser", "testuser@email.com", USER, LocalDateTime.now());
        when(authenticationService.signUp(signUpRequestDto)).thenReturn(Mono.just(userResponseDto));

        ResponseEntity<UserResponseDto> response = authenticationController.signUp(signUpRequestDto).block();

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userResponseDto, response.getBody());
        verify(authenticationService, times(1)).signUp(signUpRequestDto);
    }

    @Test
    void testSignIn_Success() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("testUser", "testPassword");
        String jwt = "test-jwt";
        when(authenticationService.signIn(signInRequestDto)).thenReturn(Mono.just(jwt));

        ResponseEntity<Void> response = authenticationController.signIn(signInRequestDto).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bearer " + jwt, response.getHeaders().getFirst("Authorization"));
        verify(authenticationService, times(1)).signIn(signInRequestDto);
    }

    @Test
    void testSignIn_BadCredentials() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("testUser", "testPassword");
        when(authenticationService.signIn(signInRequestDto)).thenReturn(Mono.error(new BadCredentialsException("Invalid credentials")));

        ResponseEntity<Void> response = authenticationController.signIn(signInRequestDto).block();

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authenticationService, times(1)).signIn(signInRequestDto);
    }
}