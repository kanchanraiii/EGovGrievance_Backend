package com.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.auth.dto.AuthRequest;
import com.auth.dto.AuthResponse;
import com.auth.dto.RegisterRequest;
import com.auth.dto.DepartmentRegisterRequest;
import com.auth.dto.UserProfileResponse;
import com.auth.service.AuthService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // register
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    // register as citizen
    @PostMapping("/citizen/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> registerCitizen(@Valid @RequestBody RegisterRequest request) {
        return authService.registerCitizen(request);
    }

    // register as case-worker
    @PostMapping("/case-worker/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> registerCaseWorker(@Valid @RequestBody DepartmentRegisterRequest request) {
        return authService.registerCaseWorker(request);
    }

    // register as admin
    @PostMapping("/admin/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        return authService.registerAdmin(request);
    }

    // register as supervisory-officer
    @PostMapping("/supervisory-officer/register")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> registerSupervisoryOfficer(@Valid @RequestBody RegisterRequest request) {
        return authService.registerSupervisoryOfficer(request);
    }

    // register as department-officer
    @PostMapping("/department-officer/register")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> registerDepartmentOfficer(@Valid @RequestBody DepartmentRegisterRequest request) {
        return authService.registerDepartmentOfficer(request);
    }

    // login endpoints
    @PostMapping("/login")
    public Mono<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/citizen/login")
    public Mono<AuthResponse> loginCitizen(@Valid @RequestBody AuthRequest request) {
        return authService.loginCitizen(request);
    }

    @PostMapping("/case-worker/login")
    public Mono<AuthResponse> loginCaseWorker(@Valid @RequestBody AuthRequest request) {
        return authService.loginCaseWorker(request);
    }

    @PostMapping("/admin/login")
    public Mono<AuthResponse> loginAdmin(@Valid @RequestBody AuthRequest request) {
        return authService.loginAdmin(request);
    }

    @PostMapping("/supervisory-officer/login")
    public Mono<AuthResponse> loginSupervisoryOfficer(@Valid @RequestBody AuthRequest request) {
        return authService.loginSupervisoryOfficer(request);
    }

    @PostMapping("/department-officer/login")
    public Mono<AuthResponse> loginDepartmentOfficer(@Valid @RequestBody AuthRequest request) {
        return authService.loginDepartmentOfficer(request);
    }

    @GetMapping("/profile")
    public Mono<UserProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return authService.currentUser(jwt.getSubject());
    }
}
