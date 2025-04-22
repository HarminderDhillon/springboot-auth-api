package com.dhillon.authapi.controller;

import com.dhillon.authapi.model.User;
import com.dhillon.authapi.model.VerificationToken;
import com.dhillon.authapi.repository.UserRepository;
import com.dhillon.authapi.security.JwtUtil;
import com.dhillon.authapi.service.EmailService;
import com.dhillon.authapi.service.UserService;
import com.dhillon.authapi.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegister() throws Exception {
        User user = new User(null, "testuser", "test@email.com", "password", true, null);
        Mockito.when(userService.findByEmail(user.email())).thenReturn(Optional.empty());
        Mockito.when(userService.registerUser(Mockito.any())).thenReturn(user);
        Mockito.when(userService.createVerificationToken(Mockito.any(), Mockito.anyString())).thenReturn(
                new VerificationToken(null, UUID.randomUUID().toString(), "userid", new java.util.Date()));
        Mockito.doNothing().when(emailService).sendVerificationEmail(Mockito.anyString(), Mockito.anyString());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testVerify() throws Exception {
        VerificationToken token = new VerificationToken("tokenid", "sometoken", "userid", new java.util.Date());
        User user = new User("userid", "testuser", "test@email.com", "password", true, null);
        Mockito.when(userService.getVerificationToken("sometoken")).thenReturn(Optional.of(token));
        Mockito.when(userRepository.findById("userid")).thenReturn(Optional.of(user));
        Mockito.doNothing().when(userService).enableUser(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/verify")
                .param("token", "sometoken"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testLogin() throws Exception {
        User user = new User("userid", "testuser", "test@email.com", "password", true, null);
        Mockito.when(userService.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        Authentication authentication = Mockito.mock(Authentication.class);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.when(jwtUtil.generateToken(Mockito.anyString(), Mockito.anyString())).thenReturn("jwtToken");

        String json = "{" +
                "\"email\":\"test@email.com\"," +
                "\"password\":\"password\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testRegisterDuplicateUsername() throws Exception {
        User user = new User(null, "testuser", "unique@email.com", "password", true, null);
        Mockito.when(userService.findByEmail(user.email())).thenReturn(Optional.empty());
        Mockito.when(userService.findByUsername(user.username())).thenReturn(Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Username already taken"));
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        User user = new User(null, "uniqueuser", "test@email.com", "password", true, null);
        Mockito.when(userService.findByEmail(user.email())).thenReturn(Optional.of(user));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Email already registered"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        Mockito.when(userService.findByEmail("wrong@email.com")).thenReturn(Optional.empty());
        String json = "{" +
                "\"email\":\"wrong@email.com\"," +
                "\"password\":\"wrongpass\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());
    }

    @Test
    void testLoginDisabledUser() throws Exception {
        User user = new User("userid", "testuser", "test@email.com", "password", false, null);
        Mockito.when(userService.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        String json = "{" +
                "\"email\":\"test@email.com\"," +
                "\"password\":\"password\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());
    }

    @Test
    void testLoginReturnsJwtToken() throws Exception {
        User user = new User("userid", "testuser", "test@email.com", "password", true, null);
        Mockito.when(userService.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        Authentication authentication = Mockito.mock(Authentication.class);
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.when(jwtUtil.generateToken(Mockito.anyString(), Mockito.anyString())).thenReturn("jwtToken");
        String json = "{" +
                "\"email\":\"test@email.com\"," +
                "\"password\":\"password\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("jwtToken"));
    }
}
