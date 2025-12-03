package com.bytebyteboot.foodapp.auth_users.services;


//   (Unit)



import com.bytebyteboot.foodapp.auth_users.dtos.LoginRequest;
import com.bytebyteboot.foodapp.auth_users.dtos.LoginResponse;
import com.bytebyteboot.foodapp.auth_users.dtos.RegistrationRequest;
import com.bytebyteboot.foodapp.auth_users.entity.User;
import com.bytebyteboot.foodapp.auth_users.repository.UserRepository;
import com.bytebyteboot.foodapp.exceptions.BadRequestException;
import com.bytebyteboot.foodapp.exceptions.NotFoundException;
import com.bytebyteboot.foodapp.response.Response;
import com.bytebyteboot.foodapp.role.entity.Role;
import com.bytebyteboot.foodapp.role.repository.RoleRepository;
import com.bytebyteboot.foodapp.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegistrationRequest registrationRequest;
    private LoginRequest loginRequest;
    private User user;
    private Role customerRole;


    @BeforeEach
    void setUp() {
        // Setup test data
        customerRole = Role.builder()
                .id(1L)
                .name("CUSTOMER")
                .build();

        registrationRequest = new RegistrationRequest();
        registrationRequest.setName("Test User");
        registrationRequest.setEmail("lokeshkumawat1225@gmail.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setPhoneNumber("1234567890");
        registrationRequest.setAddress("Test Address");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("lokeshkumawat1225@gmail.com");
        loginRequest.setPassword("password123");

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("lokeshkumawat1225@gmail.com")
                .password("encodedPassword")
                .isActive(true)
                .roles(List.of(customerRole))
                .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegister_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        Response<?> response = authService.register(registrationRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("User Registered Successfully");

        verify(userRepository).existsByEmail("lokeshkumawat1225@gmail.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }


    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegister_EmailExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registrationRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already exists");

        verify(userRepository).existsByEmail("lokeshkumawat1225@gmail.com");
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.generateToken(anyString())).thenReturn("jwt-token");

        // When
        Response<LoginResponse> response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Login Successful");
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getToken()).isEqualTo("jwt-token");
        assertThat(response.getData().getRoles()).contains("CUSTOMER");

        verify(userRepository).findByEmail("lokeshkumawat1225@gmail.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtils).generateToken("lokeshkumawat1225@gmail.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testLogin_UserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid Email");

        verify(userRepository).findByEmail("lokeshkumawat1225@gmail.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when password is invalid")
    void testLogin_InvalidPassword() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid Password");

        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should throw exception when account is not active")
    void testLogin_AccountNotActive() {
        // Given
        user.setActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Account not active, Please contact customer support");

        verify(userRepository).findByEmail("lokeshkumawat1225@gmail.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }


}
