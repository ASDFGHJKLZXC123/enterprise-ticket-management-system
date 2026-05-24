package com.example.tickets.repository;

import com.example.tickets.entity.Role;
import com.example.tickets.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> findByRoleInAndActiveTrueOrderByFullNameAsc(List<Role> roles);
}

