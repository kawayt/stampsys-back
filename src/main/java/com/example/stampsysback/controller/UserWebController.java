package com.example.stampsysback.controller;

import com.example.stampsysback.model.User;
import com.example.stampsysback.service.UserService;
import com.example.stampsysback.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserWebController {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserWebController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "app";
    }

    @PostMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateRole(@PathVariable("id") Integer id, @RequestParam("role") String role, Model model) {
        userService.updateRole(id, role);
        return "redirect:/users";
    }

    @GetMapping("/users/admins/count")
    @ResponseBody
    public Long adminCount() {
        return userRepository.countByRole("ADMIN");
    }
}