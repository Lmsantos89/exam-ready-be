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

import java.util.UUID;

import static com.lms.examready.model.Role.USER;
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


    /**
     * Test successful sign-in with valid credentials
     * This test verifies that when a user signs in with valid credentials,
     * the method returns a JWT token.
     */
    @Test
    void testSignInSuccessWithValidCredentials() {
        UUID userId = UUID.randomUUID();

        SignInRequestDto signInRequestDto = new SignInRequestDto("validUser", "validPassword");
        User user = new User();
        user.setId(userId);
        user.setUsername("validUser");
        user.setPassword("encodedPassword");
        user.setRole(USER);
        user.setEnabled(true);

        when(userService.findByUsername("validUser")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("validPassword", "encodedPassword")).thenReturn(true);
        when(jwtProvider.generateToken(userId, "validUser", USER)).thenReturn("jwtToken");

        Mono<String> result = authenticationService.signIn(signInRequestDto);

        StepVerifier.create(result)
                .expectNext("jwtToken")
                .verifyComplete();

        verify(userService).findByUsername("validUser");
        verify(passwordEncoder).matches("validPassword", "encodedPassword");
        verify(jwtProvider).generateToken(userId, "validUser", USER);
    }

    /**
     * Tests the signIn method with a disabled user account.
     * Expects a BadCredentialsException to be thrown.
     */
    @Test
    void testSignInWithDisabledAccount() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("disabledUser", "password");
        User disabledUser = new User();
        disabledUser.setEnabled(false);
        when(userService.findByUsername(signInRequestDto.username())).thenReturn(Mono.just(disabledUser));

        StepVerifier.create(authenticationService.signIn(signInRequestDto))
                .expectErrorMatches(throwable -> throwable instanceof BadCredentialsException &&
                        throwable.getMessage().equals("Account is disabled"))
                .verify();

        verify(userService, times(1)).findByUsername(signInRequestDto.username());
        verifyNoInteractions(passwordEncoder, jwtProvider);
    }

    /**
     * Tests the signIn method with incorrect password.
     * Expects a BadCredentialsException to be thrown.
     */
    @Test
    void testSignInWithIncorrectPassword() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("validUser", "incorrectPassword");
        User validUser = new User();
        validUser.setEnabled(true);
        validUser.setPassword("correctPassword");
        when(userService.findByUsername(signInRequestDto.username())).thenReturn(Mono.just(validUser));
        when(passwordEncoder.matches(signInRequestDto.password(), validUser.getPassword())).thenReturn(false);

        StepVerifier.create(authenticationService.signIn(signInRequestDto))
                .expectErrorMatches(throwable -> throwable instanceof BadCredentialsException &&
                        throwable.getMessage().equals("Invalid username or password"))
                .verify();

        verify(userService, times(1)).findByUsername(signInRequestDto.username());
        verify(passwordEncoder, times(1)).matches(signInRequestDto.password(), validUser.getPassword());
        verifyNoInteractions(jwtProvider);
    }

    /**
     * Tests the signIn method with a non-existent username.
     * Expects a BadCredentialsException to be thrown.
     */
    @Test
    void testSignInWithNonExistentUsername() {
        SignInRequestDto signInRequestDto = new SignInRequestDto("nonexistent", "password");
        when(userService.findByUsername(signInRequestDto.username())).thenReturn(Mono.empty());

        StepVerifier.create(authenticationService.signIn(signInRequestDto))
                .expectErrorMatches(throwable -> throwable instanceof BadCredentialsException &&
                        throwable.getMessage().equals("Invalid username or password"))
                .verify();

        verify(userService, times(1)).findByUsername(signInRequestDto.username());
        verifyNoInteractions(passwordEncoder, jwtProvider);
    }

    /**
     * Test case for signUp method when the username already exists.
     * It should return a Mono that emits a UserAlreadyExistsException.
     */
    @Test
    void testSignUpWithExistingUsername() {
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("existingUser", "password", "email@example.com");
        User existingUser = new User();
        existingUser.setUsername("existingUser");

        when(userService.findByUsername(signUpRequestDto.username()))
                .thenReturn(Mono.just(existingUser));

        Mono<UserResponseDto> result = authenticationService.signUp(signUpRequestDto);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof UserAlreadyExistsException &&
                        throwable.getMessage().equals("Username 'existingUser' already exists"))
                .verify();

        verify(userService, times(1)).findByUsername(signUpRequestDto.username());
        verify(userService, never()).saveUser(any());
    }

    /**
     * Tests the signUp method when attempting to register a user with an existing username.
     * This test verifies that the method throws a UserAlreadyExistsException when the username is already taken.
     */
    @Test
    void test_signUp_existingUsername_throwsUserAlreadyExistsException() {
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
}
