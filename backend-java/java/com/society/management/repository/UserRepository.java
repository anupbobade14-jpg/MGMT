package com.society.management.repository;

import com.society.management.entity.User;
import com.society.management.entity.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    @Query("select count(u) from User u where u.role = com.society.management.entity.Role.OWNER")
    long countOwners();
}
