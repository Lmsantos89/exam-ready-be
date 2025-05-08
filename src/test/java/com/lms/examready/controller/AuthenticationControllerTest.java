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
import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import org.springframework.http.HttpHeaders;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

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

    /**
     * Tests successful sign-in scenario where the authentication service returns a valid JWT token.
     * Verifies that the controller returns a response with OK status and the correct Authorization header.
     */
    @Test
    public void testSignInSuccess() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("testUser", "testPassword");
        String jwtToken = "testJwtToken";
        when(authenticationService.signIn(signInRequestDto)).thenReturn(Mono.just(jwtToken));

        authenticationController = new AuthenticationController(authenticationService);

        ResponseEntity<Void> response = authenticationController.signIn(signInRequestDto).block();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Bearer " + jwtToken, response.getHeaders().getFirst(AUTHORIZATION));
        verify(authenticationService, times(1)).signIn(signInRequestDto);
    }

    /**
     * Tests the signIn method when invalid credentials are provided.
     * Verifies that the method returns an UNAUTHORIZED status when a BadCredentialsException is thrown.
     */
    @Test
    public void testSignIn_InvalidCredentials() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("invalidUser", "invalidPassword");
        when(authenticationService.signIn(signInRequestDto)).thenReturn(Mono.error(new BadCredentialsException("Invalid credentials")));

        ResponseEntity<Void> response = authenticationController.signIn(signInRequestDto).block();

        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(authenticationService, times(1)).signIn(signInRequestDto);
    }

    /**
     * Tests the signUp method of AuthenticationController for successful user registration.
     * Verifies that the controller returns a ResponseEntity with CREATED status and the correct UserResponseDto.
     */
    @Test
    public void testSignUpSuccessful() {
        // Arrange
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("testUser", "password123", "test@email.com");
        UserResponseDto expectedUserResponseDto = new UserResponseDto( "testUser", "test@email.com", USER, now());
        when(authenticationService.signUp(signUpRequestDto)).thenReturn(Mono.just(expectedUserResponseDto));

        authenticationController = new AuthenticationController(authenticationService);

        // Act
        ResponseEntity<UserResponseDto> response = authenticationController.signUp(signUpRequestDto).block();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedUserResponseDto, response.getBody());
        verify(authenticationService, times(1)).signUp(signUpRequestDto);
    }
}
