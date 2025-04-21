package com.dhillon.authapi.controller;

import com.dhillon.authapi.model.User;
import com.dhillon.authapi.model.VerificationToken;
import com.dhillon.authapi.repository.UserRepository;
// import com.dhillon.authapi.security.JwtUtil;
import com.dhillon.authapi.service.EmailService;
import com.dhillon.authapi.service.UserService;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final EmailService emailService;
    // private final JwtUtil jwtUtil;
    // private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthController(UserService userService, EmailService emailService, /*JwtUtil jwtUtil, AuthenticationManager authenticationManager,*/ UserRepository userRepository) {
        this.userService = userService;
        this.emailService = emailService;
        // this.jwtUtil = jwtUtil;
        // this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.findByEmail(user.email()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        User savedUser = userService.registerUser(user);
        String token = UUID.randomUUID().toString();
        userService.createVerificationToken(savedUser, token);
        emailService.sendVerificationEmail(savedUser.email(), token);
        return ResponseEntity.ok(Map.of("message", "Registration successful. Check your email for verification."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty() || !userOpt.get().enabled()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials or email not verified"));
        }
        // Authentication authentication = authenticationManager.authenticate(
        //         new UsernamePasswordAuthenticationToken(userOpt.get().username(), password)
        // );
        // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // String jwt = jwtUtil.generateToken(userOpt.get().id(), userOpt.get().email());
        // return ResponseEntity.ok(Map.of("token", jwt));
        return ResponseEntity.ok(Map.of("message", "Login successful (authentication logic disabled)."));
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String token) {
        Optional<VerificationToken> verificationToken = userService.getVerificationToken(token);
        if (verificationToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid verification token"));
        }
        Optional<User> userOpt = userRepository.findById(verificationToken.get().userId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        userService.enableUser(userOpt.get());
        return ResponseEntity.ok(Map.of("message", "Email verified. You can now log in."));
    }
}
