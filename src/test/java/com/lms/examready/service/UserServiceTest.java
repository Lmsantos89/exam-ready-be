package com.lms.examready.service;

import com.lms.examready.dto.request.SignUpRequestDto;
import com.lms.examready.model.User;
import com.lms.examready.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static com.lms.examready.model.Role.USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    /**
     * Tests the findByUsername method with an empty username.
     * This test verifies that when an empty username is provided,
     * the method returns an empty Mono, indicating no user found.
     */
    @Test
    void testFindByUsername_EmptyUsername() {
        // Arrange
        String emptyUsername = "";
        when(userRepository.findByUsername(emptyUsername)).thenReturn(Mono.empty());

        // Act
        Mono<User> result = userService.findByUsername(emptyUsername);

        // Assert
        StepVerifier.create(result)
            .verifyComplete();

        verify(userRepository).findByUsername(emptyUsername);
    }

    /**
     * Test case for findByUsername method
     * Verifies that the method correctly returns the User Mono from the repository
     */
    @Test
    void test_findByUsername_returnsUserFromRepository() {
        String username = "testUser";
        User expectedUser = new User();
        expectedUser.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(Mono.just(expectedUser));

        Mono<User> result = userService.findByUsername(username);

        StepVerifier.create(result)
                .expectNext(expectedUser)
                .verifyComplete();
    }

    /**
     * Test case for saveUser method
     * Verifies that a new user is saved successfully with correct attributes
     */
    @Test
    void test_saveUser_successfulSave() {
        SignUpRequestDto dto = new SignUpRequestDto("testuser", "test@example.com", "password");
        User expectedUser = new User();
        expectedUser.setUsername(dto.username());
        expectedUser.setEmail(dto.email());
        expectedUser.setPassword("encodedPassword");
        expectedUser.setRole(USER);
        expectedUser.setEnabled(true);

        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(expectedUser));

        Mono<User> result = userService.saveUser(dto);

        StepVerifier.create(result)
                .expectNextMatches(user -> 
                    user.getUsername().equals(dto.username()) &&
                    user.getEmail().equals(dto.email()) &&
                    user.getPassword().equals("encodedPassword") &&
                    user.getRole().equals(USER) &&
                    user.isEnabled()
                )
                .verifyComplete();
    }
}
