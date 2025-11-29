package com.bytebyteboot.foodapp.category.services;

import com.bytebyteboot.foodapp.category.dtos.CategoryDTO;
import com.bytebyteboot.foodapp.response.Response;

import java.util.List;

public interface CategoryService {

    Response<CategoryDTO> addCategory(CategoryDTO categoryDTO);

    Response<CategoryDTO> updateCategory(CategoryDTO categoryDTO);

    Response<CategoryDTO> getCategoryById(Long id);

    Response<List<CategoryDTO>> getAllCategories();

    Response<?> deleteCategory(Long id);
}