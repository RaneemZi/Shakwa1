package com.Shakwa.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Shakwa.user.entity.Citizen;

@Repository
public interface CitizenRepo extends JpaRepository<Citizen, Long> {
    Optional<Citizen> findByEmail(String email);
    
    @Query("SELECT c FROM Citizen c WHERE CONCAT(c.firstName, ' ', c.lastName) = :name")
    Optional<Citizen> findByName(@Param("name") String name);
    
    @Query("SELECT c FROM Citizen c WHERE (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Citizen> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Pagination methods
    @Query("SELECT c FROM Citizen c WHERE (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Citizen> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
} 