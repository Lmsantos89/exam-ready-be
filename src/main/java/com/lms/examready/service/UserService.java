package com.lms.examready.service;

import com.lms.examready.dto.request.SignUpRequestDto;
import com.lms.examready.model.User;
import com.lms.examready.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.lms.examready.model.Role.USER;
import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class UserService

{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Mono<User> saveUser(SignUpRequestDto dto) {
            User user = new User();
            user.setUsername(dto.username());
            user.setEmail(dto.email());
            user.setPassword(passwordEncoder.encode(dto.password()));
            user.setRole(USER);
            user.setEnabled(true);
            user.setCreatedAt(now());
            user.setUpdatedAt(now());

        return userRepository.save(user);
    }
}
