package com.bytebyteboot.foodapp.auth_users.services;

import com.bytebyteboot.foodapp.auth_users.dtos.UserDTO;
import com.bytebyteboot.foodapp.auth_users.entity.User;
import com.bytebyteboot.foodapp.response.Response;

import java.util.List;

public interface UserService {


    User getCurrentLoggedInUser();

    Response<List<UserDTO>> getAllUsers();

    Response<UserDTO> getOwnAccountDetails();

    Response<?> updateOwnAccount(UserDTO userDTO);

    Response<?> deactivateOwnAccount();

}
