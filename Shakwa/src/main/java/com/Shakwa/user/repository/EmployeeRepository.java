package com.Shakwa.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    List<Employee> findByGovernmentAgency(GovernmentAgencyType governmentAgency);
    
    @Query("SELECT e FROM Employee e WHERE e.email = :email")
    Optional<Employee> findByEmailWithGovernmentAgency(@Param("email") String email);
} 