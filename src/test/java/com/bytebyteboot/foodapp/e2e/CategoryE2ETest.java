package com.bytebyteboot.foodapp.e2e;


//END-TO-END API TESTS (REST Assured)



import com.bytebyteboot.foodapp.auth_users.dtos.LoginRequest;
import com.bytebyteboot.foodapp.auth_users.dtos.RegistrationRequest;
import com.bytebyteboot.foodapp.category.dtos.CategoryDTO;
import com.bytebyteboot.foodapp.role.entity.Role;
import com.bytebyteboot.foodapp.role.repository.RoleRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Category E2E Tests")
class CategoryE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private RoleRepository roleRepository;

    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api";

        // Create roles
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ADMIN").build());
        }
        if (roleRepository.findByName("CUSTOMER").isEmpty()) {
            roleRepository.save(Role.builder().name("CUSTOMER").build());
        }

        // Register and login admin user
        adminToken = registerAndLoginAdmin();
    }

    @Test
    @DisplayName("Should get all categories")
    void testGetAllCategories() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/categories")
                .then()
                .statusCode(200)
                .body("statusCode", equalTo(200))
                .body("message", equalTo("All categories retrieved successfully"))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("Should create category with admin role")
    void testCreateCategory_AsAdmin() {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Italian");
        categoryDTO.setDescription("Italian cuisine");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(categoryDTO)
                .when()
                .post("/categories")
                .then()
                .statusCode(200)
                .body("statusCode", equalTo(200))
                .body("message", equalTo("Category added successfully"));
    }

    @Test
    @DisplayName("Should return 403 when creating category without admin role")
    void testCreateCategory_WithoutAdminRole() {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Italian");
        categoryDTO.setDescription("Italian cuisine");

        given()
                .contentType(ContentType.JSON)
                .body(categoryDTO)
                .when()
                .post("/categories")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Should get category by id")
    void testGetCategoryById() {
        // First create a category
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Mexican");
        categoryDTO.setDescription("Mexican cuisine");

        Integer categoryId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(categoryDTO)
                .when()
                .post("/categories")
                .then()
                .statusCode(200)
                .extract()
                .path("data.id");

        // Then get it by id
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/categories/" + categoryId)
                .then()
                .statusCode(200)
                .body("data.name", equalTo("Mexican"));
    }

    @Test
    @DisplayName("Should update category")
    void testUpdateCategory() {
        // Create category
        CategoryDTO createDTO = new CategoryDTO();
        createDTO.setName("Chinese");
        createDTO.setDescription("Chinese cuisine");

        Integer categoryId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(createDTO)
                .when()
                .post("/categories")
                .then()
                .extract()
                .path("data.id");

        // Update category
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setId(categoryId.longValue());
        updateDTO.setName("Updated Chinese");
        updateDTO.setDescription("Updated description");

        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(updateDTO)
                .when()
                .put("/categories")
                .then()
                .statusCode(200)
                .body("message", equalTo("Category updated successfully"));
    }

    @Test
    @DisplayName("Should delete category")
    void testDeleteCategory() {
        // Create category
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Japanese");
        categoryDTO.setDescription("Japanese cuisine");

        Integer categoryId = given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body(categoryDTO)
                .when()
                .post("/categories")
                .then()
                .extract()
                .path("data.id");

        // Delete category
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/categories/" + categoryId)
                .then()
                .statusCode(200)
                .body("message", equalTo("Category deleted successfully"));
    }

    private String registerAndLoginAdmin() {
        // Register admin
        RegistrationRequest regRequest = new RegistrationRequest();
        regRequest.setName("Admin User");
        regRequest.setEmail("admin@example.com");
        regRequest.setPassword("admin123");
        regRequest.setPhoneNumber("1234567890");
        regRequest.setAddress("Admin Address");
        regRequest.setRoles(List.of("ADMIN"));

        given()
                .contentType(ContentType.JSON)
                .body(regRequest)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200);

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@example.com");
        loginRequest.setPassword("admin123");

        return given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("data.token");
    }
}
