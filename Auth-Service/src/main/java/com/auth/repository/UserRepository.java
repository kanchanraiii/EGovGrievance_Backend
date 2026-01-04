package com.auth.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.auth.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByEmail(String email);

    Flux<User> findByDepartmentIdAndRole(String departmentId, String role);

    Flux<User> findByDepartmentIdAndRoleIn(String departmentId, Iterable<String> roles);
}
