package com.example.tickets.controller;

import com.example.tickets.dto.CreateUserRequest;
import com.example.tickets.dto.UpdateUserRequest;
import com.example.tickets.dto.UserResponse;
import com.example.tickets.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/assignable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<UserResponse> listAssignableUsers() {
        return userService.listAssignableUsers();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse deactivate(@PathVariable Long id) {
        return userService.deactivate(id);
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse reactivate(@PathVariable Long id) {
        return userService.reactivate(id);
    }
}
