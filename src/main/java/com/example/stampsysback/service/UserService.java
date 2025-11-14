package com.example.stampsysback.service;

import com.example.stampsysback.dto.UserDto;
import java.util.List;

public interface UserService {
    List<UserDto> listUsers(String q);
    UserDto updateRole(Integer userId, String newRole);
}