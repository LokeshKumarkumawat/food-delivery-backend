package com.bytebyteboot.foodapp.category.repository;

import com.bytebyteboot.foodapp.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
