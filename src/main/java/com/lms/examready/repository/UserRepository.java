package com.lms.examready.repository;

import com.lms.examready.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends R2dbcRepository<User, UUID> {

    Mono<User> findByUsername(String username);
}
