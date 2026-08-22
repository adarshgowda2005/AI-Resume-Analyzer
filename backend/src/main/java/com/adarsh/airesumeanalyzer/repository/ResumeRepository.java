package com.adarsh.airesumeanalyzer.repository;

import com.adarsh.airesumeanalyzer.entity.Resume;
import com.adarsh.airesumeanalyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Resume} entities.
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * Finds all resumes uploaded by a specific user.
     *
     * @param user the user whose resumes to retrieve
     * @return list of resumes belonging to the user
     */
    List<Resume> findByUser(User user);

    /**
     * Finds all resumes uploaded by a specific user ordered by upload time descending (newest first).
     *
     * @param user the user whose resumes to retrieve
     * @return list of resumes belonging to the user sorted by upload timestamp descending
     */
    List<Resume> findByUserOrderByUploadedAtDesc(User user);

    /**
     * Finds a specific resume by its ID and owning user.
     *
     * @param id   the resume ID
     * @param user the owning user entity
     * @return Optional containing the matching Resume if found and owned by the user
     */
    Optional<Resume> findByIdAndUser(Long id, User user);
}
