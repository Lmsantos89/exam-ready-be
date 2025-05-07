package com.lms.examready.service;

import com.lms.examready.dto.request.SignUpRequestDto;
import com.lms.examready.model.User;
import com.lms.examready.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

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
    void findByUsername_UserExists_ReturnsUser() {
        String username = "existingUser";
        User user = new User();
        user.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Mono.just(user));

        Mono<User> result = userService.findByUsername(username);

        StepVerifier.create(result)
                .expectNextMatches(foundUser -> foundUser.getUsername().equals(username))
                .verifyComplete();
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void findByUsername_UserDoesNotExist_ReturnsEmpty() {
        String username = "nonExistentUser";
        when(userRepository.findByUsername(username)).thenReturn(Mono.empty());

        Mono<User> result = userService.findByUsername(username);

        StepVerifier.create(result)
                .verifyComplete();
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void saveUser_ValidSignUpRequest_SavesUserSuccessfully() {
        SignUpRequestDto signUpRequestDto = new SignUpRequestDto("newUser", "password", "email@example.com");
        User user = new User();
        user.setUsername(signUpRequestDto.username());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

        Mono<User> result = userService.saveUser(signUpRequestDto);

        StepVerifier.create(result)
                .expectNextMatches(savedUser -> savedUser.getUsername().equals(signUpRequestDto.username()))
                .verifyComplete();
        verify(userRepository, times(1)).save(any(User.class));
    }
}
