package com.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.auth.dto.AuthRequest;
import com.auth.dto.DepartmentRegisterRequest;
import com.auth.dto.RegisterRequest;
import com.auth.model.User;
import com.auth.model.UserRole;
import com.auth.repository.UserRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private DepartmentCatalog departmentCatalog;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private Instant fixedExpiry;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .fullName("John Doe")
                .email("USER@example.com")
                .phone("1234567890")
                .password("password123")
                .departmentId("dept-1")
                .build();
        fixedExpiry = Instant.parse("2026-01-01T00:00:00Z");

        when(passwordEncoder.encode(anyString())).thenReturn("encoded-pass");
        when(jwtService.buildExpiry()).thenReturn(fixedExpiry);
        when(jwtService.generateToken(any(User.class), any())).thenReturn(Mono.just("jwt-token"));
        when(departmentCatalog.isValid(anyString())).thenReturn(true);
    }

    @Test
    void registerCitizenCreatesUser() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Mono.empty());
        doAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setId("generated-id");
            return Mono.just(toSave);
        }).when(userRepository).save(any(User.class));

        StepVerifier.create(authService.registerCitizen(registerRequest))
                .verifyComplete();

        verify(userRepository).findByEmail("user@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerCaseWorkerRequiresDepartmentId() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("case@example.com")
                .phone("9876543210")
                .password("secret123")
                .departmentId(null)
                .build();

        StepVerifier.create(authService.registerCaseWorkerForDepartment(request, null))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .verify();

        verifyNoInteractions(userRepository);
    }

    @Test
    void registerDepartmentOfficerValidatesDepartmentAndUniqueness() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.empty());
        when(departmentCatalog.isValid("DEPT-1")).thenReturn(true);
        when(userRepository.findByDepartmentIdAndRole("DEPT-1", UserRole.DEPARTMENT_OFFICER.value()))
                .thenReturn(reactor.core.publisher.Flux.just(new User()));

        DepartmentRegisterRequest request = new DepartmentRegisterRequest();
        request.setFullName("Officer One");
        request.setEmail("officer@example.com");
        request.setPhone("1112223333");
        request.setPassword("password123");
        request.setDepartmentId("dept-1");

        StepVerifier.create(authService.registerDepartmentOfficer(request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verify();
    }

    @Test
    void registerDepartmentOfficerRejectsUnknownDepartment() {
        when(departmentCatalog.isValid(anyString())).thenReturn(false);

        DepartmentRegisterRequest request = new DepartmentRegisterRequest();
        request.setFullName("Officer Two");
        request.setEmail("officer2@example.com");
        request.setPhone("1112223333");
        request.setPassword("password123");
        request.setDepartmentId("missing");

        StepVerifier.create(authService.registerDepartmentOfficer(request))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                })
                .verify();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Mono.just(new User()));

        StepVerifier.create(authService.registerCitizen(registerRequest))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                })
                .verify();
    }

    @Test
    void loginRejectsBadPassword() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("hashed");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        StepVerifier.create(authService.login(new AuthRequest("user@example.com", "wrong")))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                })
                .verify();
    }

    @Test
    void loginRoleMismatchIsForbidden() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setPassword("hash");
        user.setRole(UserRole.CITIZEN.value());

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(User.class), any())).thenReturn(Mono.just("jwt"));

        StepVerifier.create(authService.loginAdmin(new AuthRequest("admin@example.com", "pwd")))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                })
                .verify();
    }

    @Test
    void loginPropagatesJwtFailures() {
        User user = new User();
        user.setId("user-id");
        user.setEmail("user@example.com");
        user.setPassword("hash");
        user.setFullName("User");
        user.setPhone("12345");
        user.setRole(UserRole.CITIZEN.value());

        when(userRepository.findByEmail("user@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(User.class), any())).thenReturn(Mono.error(new IllegalArgumentException("jwt error")));

        StepVerifier.create(authService.login(new AuthRequest("user@example.com", "pwd")))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .verify();
    }

    @Test
    void currentUserReturnsProfile() {
        User user = new User();
        user.setId("user-id");
        user.setEmail("user@example.com");
        user.setFullName("User");
        user.setPhone("12345");
        user.setRole(UserRole.ADMIN.value());
        user.setDepartmentId("D1");

        when(userRepository.findById("user-id")).thenReturn(Mono.just(user));

        StepVerifier.create(authService.currentUser("user-id"))
                .assertNext(profile -> {
                    assertThat(profile.getUserId()).isEqualTo("user-id");
                    assertThat(profile.getEmail()).isEqualTo("user@example.com");
                    assertThat(profile.getDepartmentId()).isEqualTo("D1");
                })
                .verifyComplete();
    }

    @Test
    void currentUserNotFound() {
        when(userRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(authService.currentUser("missing"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                })
                .verify();
    }

    @Test
    void registerRejectsMissingRequiredFields() {
        RegisterRequest invalid = new RegisterRequest();

        StepVerifier.create(authService.registerCitizen(invalid))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                })
                .verify();
    }

    @Test
    void registerDelegatesToCitizenPath() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("id-1");
            return Mono.just(user);
        });

        StepVerifier.create(authService.register(registerRequest))
                .verifyComplete();
    }

    @Test
    void registerCaseWorkerCompletesWhenDepartmentValid() {
        DepartmentRegisterRequest request = new DepartmentRegisterRequest();
        request.setFullName("Worker");
        request.setEmail("worker@example.com");
        request.setPhone("99999");
        request.setPassword("pwd");
        request.setDepartmentId("dept-1");

        when(userRepository.findByEmail("worker@example.com")).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("id-2");
            return Mono.just(user);
        });

        StepVerifier.create(authService.registerCaseWorker(request))
                .verifyComplete();
    }

    @Test
    void registerCaseWorkerWithoutDepartmentIsRejected() {
        DepartmentRegisterRequest request = new DepartmentRegisterRequest();
        request.setFullName("Worker");
        request.setEmail("worker@example.com");
        request.setPhone("99999");
        request.setPassword("pwd");
        request.setDepartmentId("  ");

        StepVerifier.create(authService.registerCaseWorker(request))
                .expectErrorSatisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST))
                .verify();
    }

    @Test
    void registerRejectsWhenFullNameMissingButEmailPresent() {
        RegisterRequest request = RegisterRequest.builder()
                .email("user@example.com")
                .password("pwd")
                .phone("123")
                .build();

        StepVerifier.create(authService.register(request))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void registerRejectsWhenPhoneMissing() {
        RegisterRequest request = RegisterRequest.builder()
                .email("user@example.com")
                .fullName("User")
                .password("pwd")
                .build();

        StepVerifier.create(authService.registerCitizen(request))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void registerRejectsWhenPasswordMissing() {
        RegisterRequest request = RegisterRequest.builder()
                .email("user@example.com")
                .fullName("User")
                .phone("123")
                .build();

        StepVerifier.create(authService.register(request))
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    void ensureDepartmentOfficerUniqueHandlesNullDepartmentId() throws Exception {
        java.lang.reflect.Method method = AuthService.class
                .getDeclaredMethod("ensureDepartmentOfficerUnique", UserRole.class, String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Mono<User> result = (Mono<User>) method.invoke(authService, UserRole.DEPARTMENT_OFFICER, null);

        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void registerCaseWorkerForDepartmentUsesTokenDepartment() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("id-3");
            return Mono.just(user);
        });

        StepVerifier.create(authService.registerCaseWorkerForDepartment(registerRequest, "DPT-5"))
                .verifyComplete();
    }

    @Test
    void registerAdminAndSupervisoryOfficerSucceedWithoutDepartment() {
        RegisterRequest adminRequest = RegisterRequest.builder()
                .fullName("Admin")
                .email("admin@example.com")
                .phone("11111")
                .password("pwd")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("id-4");
            return Mono.just(user);
        });

        StepVerifier.create(authService.registerAdmin(adminRequest)).verifyComplete();
        StepVerifier.create(authService.registerSupervisoryOfficer(adminRequest)).verifyComplete();
    }

    @Test
    void loginVariantsReturnTokensForMatchingRoles() {
        User citizen = new User();
        citizen.setId("c1");
        citizen.setEmail("citizen@example.com");
        citizen.setPassword("hash");
        citizen.setFullName("Citizen");
        citizen.setPhone("123");
        citizen.setRole(UserRole.CITIZEN.value());

        when(userRepository.findByEmail("citizen@example.com")).thenReturn(Mono.just(citizen));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(User.class), any())).thenReturn(Mono.just("token-1"));

        StepVerifier.create(authService.loginCitizen(new AuthRequest("citizen@example.com", "pwd")))
                .assertNext(response -> {
                    assertThat(response.getToken()).isEqualTo("token-1");
                    assertThat(response.getUserId()).isEqualTo("c1");
                })
                .verifyComplete();
    }

    @Test
    void loginOtherRolesReturnTokens() {
        User worker = new User();
        worker.setId("w1");
        worker.setEmail("worker@example.com");
        worker.setPassword("hash");
        worker.setFullName("Worker");
        worker.setPhone("123");
        worker.setRole(UserRole.CASE_WORKER.value());

        when(userRepository.findByEmail("worker@example.com")).thenReturn(Mono.just(worker));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(User.class), any())).thenReturn(Mono.just("token-2"));

        StepVerifier.create(authService.loginCaseWorker(new AuthRequest("worker@example.com", "pwd")))
                .expectNextMatches(resp -> resp.getToken().equals("token-2"))
                .verifyComplete();

        worker.setRole(UserRole.SUPERVISORY_OFFICER.value());
        when(jwtService.generateToken(any(User.class), any())).thenReturn(Mono.just("token-3"));
        StepVerifier.create(authService.loginSupervisoryOfficer(new AuthRequest("worker@example.com", "pwd")))
                .expectNextMatches(resp -> resp.getToken().equals("token-3"))
                .verifyComplete();

        worker.setRole(UserRole.DEPARTMENT_OFFICER.value());
        when(jwtService.generateToken(any(User.class), any())).thenReturn(Mono.just("token-4"));
        StepVerifier.create(authService.loginDepartmentOfficer(new AuthRequest("worker@example.com", "pwd")))
                .expectNextMatches(resp -> resp.getToken().equals("token-4"))
                .verifyComplete();
    }
}
