package com.example.stampsysback.repository;

import com.example.stampsysback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByProviderUserId(String providerUserId);
    Optional<User> findByEmail(String email);

    Page<User> findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String userName, String email, Pageable pageable);
}