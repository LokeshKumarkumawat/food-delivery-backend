package com.bytebyteboot.foodapp.auth_users.repository;

//(Integration)


import com.bytebyteboot.foodapp.auth_users.entity.User;
import com.bytebyteboot.foodapp.role.entity.Role;
import com.bytebyteboot.foodapp.role.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        customerRole = Role.builder()
                .name("CUSTOMER")
                .build();
        roleRepository.save(customerRole);

        testUser = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .phoneNumber("1234567890")
                .address("Test Address")
                .roles(List.of(customerRole))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should save user successfully")
    void testSaveUser() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should find user by email")
    void testFindByEmail() {
        // Given
        userRepository.save(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void testFindByEmail_NotFound() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("notfound@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        // Given
        userRepository.save(testUser);

        // When & Then
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notfound@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        // Given
        User user2 = User.builder()
                .name("User 2")
                .email("user2@example.com")
                .password("password")
                .roles(List.of(customerRole))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(testUser);
        userRepository.save(user2);

        // When
        List<User> users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(2);
    }

}
