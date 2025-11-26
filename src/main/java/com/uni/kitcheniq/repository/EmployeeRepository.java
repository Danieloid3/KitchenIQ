package com.uni.kitcheniq.repository;

import com.uni.kitcheniq.models.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Optional<Employee> findById(String id);

    @Query("SELECT e FROM Employee e")
    Optional<List<Employee>> getAllEmployees();

    boolean existsByIdNumber(String idNumber);

    @Query("SELECT e FROM Employee e WHERE e.type IN (1, 2)")
    List<Employee> findAllEmployeePositions();

    @Query("SELECT CASE WHEN e.type IN (1, 2) THEN true ELSE false END FROM Employee e WHERE e.id = :id")
    boolean isEmployeePosition(@Param("id") String id);

}
