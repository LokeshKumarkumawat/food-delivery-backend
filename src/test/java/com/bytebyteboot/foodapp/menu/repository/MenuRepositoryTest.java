package com.bytebyteboot.foodapp.menu.repository;


import com.bytebyteboot.foodapp.category.entity.Category;
import com.bytebyteboot.foodapp.category.repository.CategoryRepository;
import com.bytebyteboot.foodapp.menu.entity.Menu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Menu Repository Tests")
class MenuRepositoryTest {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;
    private Menu menu1;
    private Menu menu2;

    @BeforeEach
    void setUp() {
        menuRepository.deleteAll();
        categoryRepository.deleteAll();

        category = Category.builder()
                .name("Fast Food")
                .description("Quick meals")
                .build();
        categoryRepository.save(category);

        menu1 = Menu.builder()
                .name("Pizza")
                .description("Delicious pizza")
                .price(BigDecimal.valueOf(15.99))
                .category(category)
                .imageUrl("pizza.jpg")
                .build();

        menu2 = Menu.builder()
                .name("Burger")
                .description("Juicy burger")
                .price(BigDecimal.valueOf(9.99))
                .category(category)
                .imageUrl("burger.jpg")
                .build();
    }

    @Test
    @DisplayName("Should save menu successfully")
    void testSaveMenu() {
        // When
        Menu savedMenu = menuRepository.save(menu1);

        // Then
        assertThat(savedMenu).isNotNull();
        assertThat(savedMenu.getId()).isNotNull();
        assertThat(savedMenu.getName()).isEqualTo("Pizza");
    }

    @Test
    @DisplayName("Should find menu by id")
    void testFindById() {
        // Given
        Menu saved = menuRepository.save(menu1);

        // When
        Menu found = menuRepository.findById(saved.getId()).orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Pizza");
    }

    @Test
    @DisplayName("Should find all menus")
    void testFindAll() {
        // Given
        menuRepository.save(menu1);
        menuRepository.save(menu2);

        // When
        List<Menu> menus = menuRepository.findAll();

        // Then
        assertThat(menus).hasSize(2);
    }

    @Test
    @DisplayName("Should delete menu")
    void testDeleteMenu() {
        // Given
        Menu saved = menuRepository.save(menu1);

        // When
        menuRepository.deleteById(saved.getId());

        // Then
        assertThat(menuRepository.findById(saved.getId())).isEmpty();
    }
}