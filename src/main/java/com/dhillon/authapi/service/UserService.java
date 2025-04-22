package com.dhillon.authapi.service;

import com.dhillon.authapi.model.User;
import com.dhillon.authapi.model.VerificationToken;
import com.dhillon.authapi.repository.UserRepository;
import com.dhillon.authapi.repository.VerificationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, VerificationTokenRepository tokenRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        User updatedUser = new User(
            user.id(),
            user.username(),
            user.email(),
            passwordEncoder.encode(user.password()),
            false,
            user.roles()
        );
        return userRepository.save(updatedUser);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public VerificationToken createVerificationToken(User user, String token) {
        VerificationToken verificationToken = new VerificationToken(
            null,
            token,
            user.id(),
            new java.util.Date(System.currentTimeMillis() + 86400000)
        );
        return tokenRepository.save(verificationToken);
    }

    public Optional<VerificationToken> getVerificationToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public void enableUser(User user) {
        User enabledUser = new User(
            user.id(),
            user.username(),
            user.email(),
            user.password(),
            true,
            user.roles()
        );
        userRepository.save(enabledUser);
    }
}
