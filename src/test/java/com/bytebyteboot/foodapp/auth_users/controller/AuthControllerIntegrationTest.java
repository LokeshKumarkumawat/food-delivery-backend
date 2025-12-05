package com.bytebyteboot.foodapp.auth_users.controller;

//(Integration)



import com.bytebyteboot.foodapp.auth_users.dtos.LoginRequest;
import com.bytebyteboot.foodapp.auth_users.dtos.RegistrationRequest;
import com.bytebyteboot.foodapp.auth_users.repository.UserRepository;
import com.bytebyteboot.foodapp.role.entity.Role;
import com.bytebyteboot.foodapp.role.repository.RoleRepository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        if (roleRepository.findByName("CUSTOMER").isEmpty()) {
            Role customerRole = Role.builder()
                    .name("CUSTOMER")
                    .build();
            roleRepository.save(customerRole);
        }
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegister_Success() throws Exception {

        RegistrationRequest request = new RegistrationRequest();
        request.setName("Test User");
        request.setEmail("lokeshkumawat1225@gmail.com");
        request.setPassword("password123");
        request.setPhoneNumber("1234567890");
        request.setAddress("Test Address");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("User Registered Successfully"));
    }

    @Test
    @DisplayName("Should return error when email already exists")
    void testRegister_EmailExists() throws Exception {

        RegistrationRequest request = new RegistrationRequest();
        request.setName("Test User");
        request.setEmail("lokeshkumawat1225@gmail.com");
        request.setPassword("password123");
        request.setPhoneNumber("1234567890");
        request.setAddress("Test Address");

        // First registration
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // Try to register again
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() throws Exception {

        RegistrationRequest regRequest = new RegistrationRequest();
        regRequest.setName("Test User");
        regRequest.setEmail("lokeshkumawat1225@gmail.com");
        regRequest.setPassword("password123");
        regRequest.setPhoneNumber("1234567890");
        regRequest.setAddress("Test Address");

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest))
        );

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("lokeshkumawat1225@gmail.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Login Successful"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    @DisplayName("Should return error with invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Email"));
    }
}
