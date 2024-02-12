package com.miraj.springboot.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.miraj.springboot.model.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
	List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrDepartmentContainingIgnoreCaseOrPositionContainingIgnoreCaseOrSalaryContainingIgnoreCase(String firstName, String lastName, String department, String position, String salary);
	List<Employee> findByDepartmentContainingIgnoreCaseOrPositionContainingIgnoreCase(String department, String position);
}
