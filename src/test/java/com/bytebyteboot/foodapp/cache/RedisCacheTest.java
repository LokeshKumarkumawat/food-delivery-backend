package com.bytebyteboot.foodapp.cache;


import com.bytebyteboot.foodapp.category.dtos.CategoryDTO;
import com.bytebyteboot.foodapp.category.entity.Category;
import com.bytebyteboot.foodapp.category.repository.CategoryRepository;
import com.bytebyteboot.foodapp.category.services.CategoryService;
import com.bytebyteboot.foodapp.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.SpyBean;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Redis Cache Tests")
class RedisCacheTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoSpyBean
    private CategoryRepository categoryRepository;

    @Autowired
    private CacheManager cacheManager;

    private Category category;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        cacheManager.getCacheNames().forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) != null) {
                cacheManager.getCache(cacheName).clear();
            }
        });

        category = Category.builder()
                .id(1L)
                .name("Fast Food")
                .description("Quick meals")
                .build();

        // Reset mock invocation counts
        reset(categoryRepository);
    }

    @Test
    @DisplayName("Should cache category on first call")
    void testGetCategoryById_CachesResult() {
        // Given
        doReturn(Optional.of(category)).when(categoryRepository).findById(1L);

        // When - First call
        Response<CategoryDTO> response1 = categoryService.getCategoryById(1L);

        // Second call - should use cache
        Response<CategoryDTO> response2 = categoryService.getCategoryById(1L);

        // Then
        assertThat(response1.getData()).isNotNull();
        assertThat(response1.getData().getName()).isEqualTo("Fast Food");
        assertThat(response2.getData()).isNotNull();
        assertThat(response2.getData().getName()).isEqualTo("Fast Food");

        // Repository should be called only once (cached on second call)
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should evict cache on update")
    void testUpdateCategory_EvictsCache() {
        // Given
        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Updated Name")
                .description("Quick meals")
                .build();

        // Use doReturn for spy to avoid calling real method
        doReturn(Optional.of(category))
                .doReturn(Optional.of(updatedCategory))
                .when(categoryRepository).findById(1L);

        // Mock save to return the same entity passed to it
        doAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            return cat;
        }).when(categoryRepository).save(any(Category.class));

        // When - Get and cache
        Response<CategoryDTO> response1 = categoryService.getCategoryById(1L);
        assertThat(response1.getData().getName()).isEqualTo("Fast Food");

        // Update - should evict cache
        CategoryDTO updateDTO = new CategoryDTO();
        updateDTO.setId(1L);
        updateDTO.setName("Updated Name");
        Response<CategoryDTO> updateResponse = categoryService.updateCategory(updateDTO);
        assertThat(updateResponse.getMessage()).isEqualTo("Category updated successfully");

        // Get again - should hit database and get updated value
        Response<CategoryDTO> response2 = categoryService.getCategoryById(1L);
        assertThat(response2.getData().getName()).isEqualTo("Updated Name");

        // Then - Repository called twice (once before update, once after)
        verify(categoryRepository, times(2)).findById(1L);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("Should evict cache on delete")
    void testDeleteCategory_EvictsCache() {
        // Given
        doReturn(Optional.of(category))
                .doReturn(Optional.of(category))
                .when(categoryRepository).findById(1L);

        doReturn(true).when(categoryRepository).existsById(1L);
        doNothing().when(categoryRepository).deleteById(anyLong());

        // When - Get and cache
        Response<CategoryDTO> response1 = categoryService.getCategoryById(1L);
        assertThat(response1.getData().getName()).isEqualTo("Fast Food");

        // Delete - should evict cache
        Response<?> deleteResponse = categoryService.deleteCategory(1L);
        assertThat(deleteResponse.getMessage()).isEqualTo("Category deleted successfully");

        // Get again - should hit database
        Response<CategoryDTO> response2 = categoryService.getCategoryById(1L);
        assertThat(response2.getData()).isNotNull();

        // Then
        verify(categoryRepository, times(2)).findById(1L);
        verify(categoryRepository, times(1)).existsById(1L);
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should cache all categories")
    void testGetAllCategories_CachesResult() {
        // Given
        Category category2 = Category.builder()
                .id(2L)
                .name("Beverages")
                .description("Drinks")
                .build();

        doReturn(java.util.List.of(category, category2))
                .when(categoryRepository).findAll();

        // When - First call
        Response<java.util.List<CategoryDTO>> response1 = categoryService.getAllCategories();

        // Second call - should use cache
        Response<java.util.List<CategoryDTO>> response2 = categoryService.getAllCategories();

        // Then
        assertThat(response1.getData()).isNotNull();
        assertThat(response1.getData()).hasSize(2);
        assertThat(response2.getData()).isNotNull();
        assertThat(response2.getData()).hasSize(2);

        // Repository should be called only once (cached on second call)
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should evict all categories cache on add")
    void testAddCategory_EvictsAllCache() {
        // Given - cache all categories first
        doReturn(java.util.List.of(category)).when(categoryRepository).findAll();

        Response<java.util.List<CategoryDTO>> response1 = categoryService.getAllCategories();
        assertThat(response1.getData()).hasSize(1);

        // Mock save for new category
        Category newCategory = Category.builder()
                .id(2L)
                .name("Beverages")
                .description("Drinks")
                .build();

        doAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            return cat;
        }).when(categoryRepository).save(any(Category.class));

        // When - Add new category (should evict cache)
        CategoryDTO newDTO = new CategoryDTO();
        newDTO.setName("Beverages");
        newDTO.setDescription("Drinks");
        categoryService.addCategory(newDTO);

        // Update mock to return 2 categories
        doReturn(java.util.List.of(category, newCategory))
                .when(categoryRepository).findAll();

        // Get all again - should hit database
        Response<java.util.List<CategoryDTO>> response2 = categoryService.getAllCategories();
        assertThat(response2.getData()).hasSize(2);

        // Then - findAll called twice (once before add, once after)
        verify(categoryRepository, times(2)).findAll();
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
}