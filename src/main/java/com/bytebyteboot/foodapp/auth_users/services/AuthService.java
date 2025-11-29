package com.bytebyteboot.foodapp.auth_users.services;

import com.bytebyteboot.foodapp.auth_users.dtos.LoginRequest;
import com.bytebyteboot.foodapp.auth_users.dtos.LoginResponse;
import com.bytebyteboot.foodapp.auth_users.dtos.RegistrationRequest;
import com.bytebyteboot.foodapp.response.Response;

public interface AuthService {
    Response<?> register(RegistrationRequest registrationRequest);
    Response<LoginResponse> login(LoginRequest loginRequest);
}