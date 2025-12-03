package com.bytebyteboot.foodapp.category.services;


import com.bytebyteboot.foodapp.category.dtos.CategoryDTO;
import com.bytebyteboot.foodapp.category.entity.Category;
import com.bytebyteboot.foodapp.category.repository.CategoryRepository;
import com.bytebyteboot.foodapp.exceptions.NotFoundException;
import com.bytebyteboot.foodapp.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .name("Fast Food")
                .description("Quick meals")
                .build();

        categoryDTO = new CategoryDTO();
        categoryDTO.setId(1L);
        categoryDTO.setName("Fast Food");
        categoryDTO.setDescription("Quick meals");
    }

    @Test
    @DisplayName("Should add category successfully")
    void testAddCategory_Success() {
        // Given
        when(modelMapper.map(any(CategoryDTO.class), eq(Category.class))).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // When
        Response<CategoryDTO> response = categoryService.addCategory(categoryDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Category added successfully");

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should get all categories successfully")
    void testGetAllCategories_Success() {
        // Given
        List<Category> categories = Arrays.asList(category);
        List<CategoryDTO> categoryDTOs = Arrays.asList(categoryDTO);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);

        // When
        Response<List<CategoryDTO>> response = categoryService.getAllCategories();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(1);

        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Should get category by id successfully")
    void testGetCategoryById_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);

        // When
        Response<CategoryDTO> response = categoryService.getCategoryById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void testGetCategoryById_NotFound() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category Not Found");

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should update category successfully")
    void testUpdateCategory_Success() {
        // Given
        categoryDTO.setName("Updated Name");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // When
        Response<CategoryDTO> response = categoryService.updateCategory(categoryDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(category.getName()).isEqualTo("Updated Name");

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should delete category successfully")
    void testDeleteCategory_Success() {
        // Given
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        // When
        Response<?> response = categoryService.deleteCategory(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Category deleted successfully");

        verify(categoryRepository).deleteById(1L);
    }
}