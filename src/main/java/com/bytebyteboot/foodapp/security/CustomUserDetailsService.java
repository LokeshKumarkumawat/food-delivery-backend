package com.bytebyteboot.foodapp.security;

import com.bytebyteboot.foodapp.auth_users.entity.User;
import com.bytebyteboot.foodapp.auth_users.repository.UserRepository;
import com.bytebyteboot.foodapp.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(username)
                .orElseThrow(()-> new NotFoundException("User not found"));

        return AuthUser.builder()   // this AuthUser Comes Feom AuthUser.Java in same package
                .user(user)
                .build();
    }
}