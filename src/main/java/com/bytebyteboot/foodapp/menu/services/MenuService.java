package com.bytebyteboot.foodapp.menu.services;

import com.bytebyteboot.foodapp.menu.dtos.MenuDTO;
import com.bytebyteboot.foodapp.response.Response;

import java.util.List;

public interface MenuService {

    Response<MenuDTO> createMenu(MenuDTO menuDTO);
    Response<MenuDTO> updateMenu(MenuDTO menuDTO);
    Response<MenuDTO> getMenuById(Long id);
    Response<?> deleteMenu(Long id);
    Response<List<MenuDTO>> getMenus(Long categoryId, String search);

}
