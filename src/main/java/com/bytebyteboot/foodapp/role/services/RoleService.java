package com.bytebyteboot.foodapp.role.services;


import com.bytebyteboot.foodapp.response.Response;
import com.bytebyteboot.foodapp.role.dtos.RoleDTO;

import java.util.List;

public interface RoleService {

    Response<RoleDTO> createRole(RoleDTO roleDTO);

    Response<RoleDTO> updateRole(RoleDTO roleDTO);

    Response<List<RoleDTO>> getAllRoles();

    Response<?> deleteRole(Long id);
}
