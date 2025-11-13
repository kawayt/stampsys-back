package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserDto;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserDto> listUsers(int page, int size, String sortBy, String direction, String q);
    UserDto updateRole(Integer userId, String newRole);
}