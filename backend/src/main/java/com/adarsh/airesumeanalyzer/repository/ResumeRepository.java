package com.adarsh.airesumeanalyzer.repository;

import com.adarsh.airesumeanalyzer.entity.Resume;
import com.adarsh.airesumeanalyzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
