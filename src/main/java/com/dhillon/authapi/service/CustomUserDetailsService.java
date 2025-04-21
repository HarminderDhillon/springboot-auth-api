package com.dhillon.authapi.service;

import com.dhillon.authapi.model.User;
import com.dhillon.authapi.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.username())
                .password(user.password())
                .authorities(user.roles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()))
                .accountLocked(!user.enabled())
                .build();
    }

    public UserDetails loadUserById(String id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.username())
                .password(user.password())
                .authorities(user.roles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()))
                .accountLocked(!user.enabled())
                .build();
    }
}
