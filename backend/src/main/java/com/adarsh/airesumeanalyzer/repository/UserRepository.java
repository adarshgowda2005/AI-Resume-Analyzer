package com.adarsh.airesumeanalyzer.repository;

import com.adarsh.airesumeanalyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * <p>
 * Provides CRUD operations, pagination, sorting, and custom query methods
 * backed by Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the found {@link User}, or {@link Optional#empty()} if no user exists with the given email
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email the email address to verify
     * @return {@code true} if a user with the specified email exists, {@code false} otherwise
     */
    boolean existsByEmail(String email);
}
