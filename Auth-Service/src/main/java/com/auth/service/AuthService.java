package com.auth.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.auth.dto.AuthRequest;
import com.auth.dto.AuthResponse;
import com.auth.dto.RegisterRequest;
import com.auth.dto.DepartmentRegisterRequest;
import com.auth.dto.UserProfileResponse;
import com.auth.model.User;
import com.auth.model.UserRole;
import com.auth.repository.UserRepository;
import com.auth.service.DepartmentCatalog;

import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final DepartmentCatalog departmentCatalog;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, DepartmentCatalog departmentCatalog) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.departmentCatalog = departmentCatalog;
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        return registerCitizen(request);
    }

    
    // add methods for login/register as per roles
    public Mono<AuthResponse> registerCitizen(RegisterRequest request) {
        return registerWithRole(request, UserRole.CITIZEN);
    }

    public Mono<AuthResponse> registerCaseWorker(RegisterRequest request) {
        return registerWithRole(request, UserRole.CASE_WORKER);
    }

    public Mono<AuthResponse> registerCaseWorker(DepartmentRegisterRequest request) {
        return registerWithRole(request, UserRole.CASE_WORKER);
    }

    public Mono<AuthResponse> registerAdmin(RegisterRequest request) {
        return registerWithRole(request, UserRole.ADMIN);
    }

    public Mono<AuthResponse> registerSupervisoryOfficer(RegisterRequest request) {
        return registerWithRole(request, UserRole.SUPERVISORY_OFFICER);
    }

    public Mono<AuthResponse> registerDepartmentOfficer(RegisterRequest request) {
        return registerWithRole(request, UserRole.DEPARTMENT_OFFICER);
    }

    public Mono<AuthResponse> registerDepartmentOfficer(DepartmentRegisterRequest request) {
        return registerWithRole(request, UserRole.DEPARTMENT_OFFICER);
    }

    public Mono<AuthResponse> login(AuthRequest request) {
        return authenticate(request).flatMap(this::toAuthResponse);
    }

    public Mono<AuthResponse> loginCitizen(AuthRequest request) {
        return loginWithRole(request, UserRole.CITIZEN);
    }

    public Mono<AuthResponse> loginCaseWorker(AuthRequest request) {
        return loginWithRole(request, UserRole.CASE_WORKER);
    }

    public Mono<AuthResponse> loginAdmin(AuthRequest request) {
        return loginWithRole(request, UserRole.ADMIN);
    }

    public Mono<AuthResponse> loginSupervisoryOfficer(AuthRequest request) {
        return loginWithRole(request, UserRole.SUPERVISORY_OFFICER);
    }

    public Mono<AuthResponse> loginDepartmentOfficer(AuthRequest request) {
        return loginWithRole(request, UserRole.DEPARTMENT_OFFICER);
    }

    
    // registering users with roles
    private Mono<AuthResponse> registerWithRole(RegisterRequest request, UserRole role) {
        return registerInternal(
                request.getEmail(),
                request.getFullName(),
                request.getPhone(),
                request.getPassword(),
                request.getDepartmentId(),
                role);
    }

    private Mono<AuthResponse> registerWithRole(DepartmentRegisterRequest request, UserRole role) {
        return registerInternal(
                request.getEmail(),
                request.getFullName(),
                request.getPhone(),
                request.getPassword(),
                request.getDepartmentId(),
                role);
    }

    private Mono<AuthResponse> registerInternal(String emailRaw, String fullNameRaw, String phoneRaw, String passwordRaw, String departmentIdRaw, UserRole role) {
        if (!StringUtils.hasText(emailRaw) || !StringUtils.hasText(fullNameRaw) || !StringUtils.hasText(phoneRaw) || !StringUtils.hasText(passwordRaw)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields"));
        }

        String email = emailRaw.trim().toLowerCase();
        String fullName = fullNameRaw.trim();
        String phone = phoneRaw.trim();
        String departmentId = normalizeDepartmentId(departmentIdRaw);

        if (requiresDepartment(role)) {
            if (!StringUtils.hasText(departmentId)) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "departmentId is required"));
            }
            if (!departmentCatalog.isValid(departmentId)) {
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Department id not found"));
            }
        }

        return userRepository.findByEmail(email)
                .flatMap(existing -> Mono.<User>error(
                        new ResponseStatusException(HttpStatus.CONFLICT, "User already exists")))
                .switchIfEmpty(Mono.defer(() -> ensureDepartmentOfficerUnique(role, departmentId)))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = User.builder()
                            .fullName(fullName)
                            .phone(phone)
                            .email(email)
                            .password(passwordEncoder.encode(passwordRaw))
                            .role(role.value())
                            .departmentId(departmentId)
                            .createdAt(Instant.now())
                            .build();
                    return userRepository.save(user);
                }))
                .onErrorMap(IllegalArgumentException.class, ex ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex))
                .flatMap(this::toAuthResponse);
    }

    public Mono<UserProfileResponse> currentUser(String userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .map(user -> new UserProfileResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getPhone(),
                        user.getRole(),
                        user.getDepartmentId()));
    }

    // authenticate user
    private Mono<User> authenticate(AuthRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED, "Invalid credentials"));
                    }
                    return Mono.just(user);
                });
    }

    private Mono<AuthResponse> loginWithRole(AuthRequest request, UserRole expectedRole) {
        return authenticate(request)
                .flatMap(user -> {
                    if (!expectedRole.matches(user.getRole())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.FORBIDDEN, "User is not " + expectedRole.value()));
                    }
                    return toAuthResponse(user);
                });
    }

    private Mono<AuthResponse> toAuthResponse(User user) {
        Instant expiresAt = jwtService.buildExpiry();
        return jwtService.generateToken(user, expiresAt)
                .onErrorMap(IllegalArgumentException.class, ex ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex))
                .map(token -> new AuthResponse(
                        token,
                        expiresAt,
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getPhone(),
                        user.getRole(),
                        user.getDepartmentId()));
    }

    private String normalizeDepartmentId(String departmentId) {
        if (!StringUtils.hasText(departmentId)) {
            return null;
        }
        return departmentId.trim().toUpperCase();
    }

    private Mono<User> ensureDepartmentOfficerUnique(UserRole role, String departmentId) {
        if (role != UserRole.DEPARTMENT_OFFICER || departmentId == null) {
            return Mono.empty();
        }
        return userRepository.findByDepartmentIdAndRole(departmentId, role.value())
                .flatMap(existing -> Mono.<User>error(new ResponseStatusException(
                        HttpStatus.CONFLICT, "Department officer already exists for department " + departmentId)))
                .then(Mono.empty());
    }

    private boolean requiresDepartment(UserRole role) {
        return role == UserRole.CASE_WORKER || role == UserRole.DEPARTMENT_OFFICER;
    }
}
