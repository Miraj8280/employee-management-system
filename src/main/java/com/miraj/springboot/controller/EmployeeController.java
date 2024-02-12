package com.miraj.springboot.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.miraj.springboot.model.Employee;
import com.miraj.springboot.service.EmployeeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class EmployeeController {

	@Autowired
	private EmployeeService employeeService;
	
	public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/src/main/resources/static/images";

	// Display list of employees
	@GetMapping("/")
	public String viewHomePage(@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String departmentPositionFilter,
			@RequestParam(defaultValue = "1") int pageNo,
			@RequestParam(defaultValue = "firstName") String sortField,
			@RequestParam(defaultValue = "asc") String sortDir, 
			Model model) {

		//Split the received value into department and position
		
	    String[] filters = departmentPositionFilter != null ? departmentPositionFilter.split("\\s+") : new String[0];

	    String departmentFilter = "";
	    String positionFilter = "";

	    if (filters.length > 0) {
	        departmentFilter = filters[0];
	    }

	    if (filters.length > 1) {
	        positionFilter = filters[1];
	    }
	    
		if (keyword != null) {
	        // Search mode
	        return search(keyword, pageNo, sortField, sortDir, model);
	    } else if (departmentPositionFilter != null) {
	        // Filter mode
	        return filterByDepartmentOrPosition(departmentFilter, positionFilter, pageNo, sortField, sortDir, model);
	    } else {
	        // Normal display mode
	        return findPaginated(pageNo, sortField, sortDir, model);
	    }
	}

	// Add new employee
	@GetMapping("/showNewEmployeeForm")
	public String showNewEmployeeForm(Model model) {
		// Create model attribute to bind from data
		Employee employee = new Employee();
		model.addAttribute("employee", employee);
		return "new_employee";
	}

	// Save new employee and image
	@PostMapping("/saveEmployee")
	public String saveEmployee(@ModelAttribute("employee") Employee employee,
	                           @RequestParam("img") MultipartFile img, 
	                           HttpSession session) {

	    try {
	        // Save the profile picture file
	        if (!img.isEmpty()) {
	            // Generate image name based on the employee's full name
	            String imageName = generateImageName(employee);
	            Path fileNameAndPath = Paths.get(EmployeeController.UPLOAD_DIRECTORY, imageName);
	            Files.write(fileNameAndPath, img.getBytes());
	            // Set the profile picture file name in the employee object
	            employee.setProfilePictureFileName(imageName);
	        }   

	        // Save employee to database
	        employeeService.saveEmployee(employee);

	        session.setAttribute("msg", "Employee and Image Successfully Uploaded!");

	    } catch (IOException e) {
	        // Handle exception if file upload fails
	        e.printStackTrace(); // You might want to log the exception
	        session.setAttribute("msg", "Error uploading image.");
	    }

	    return "redirect:/";
	}

	private String generateImageName(Employee employee) {
	    // Generate name based on the employee's full name
	    String fullName = employee.getFirstName() + "_" + employee.getLastName();
	    return fullName.toLowerCase() + ".jpg";
	}


	// Update form
	@GetMapping("/showFormForUpdate/{id}")
	public String showFormForUpdate(@PathVariable(value = "id") long id, Model model) {
		// Get employee from the service
		Employee employee = employeeService.getEmployeeById(id);

		// Set employee as a model attribute to pre-populate the form
		model.addAttribute("employee", employee);

		return "update_employee";
	}

	// Delete employee
	@GetMapping("/deleteEmployee/{id}")
	public String deleteEmployee(@PathVariable(value = "id") long id) {
		// Call delete employee method
		this.employeeService.deleteEmployeeById(id);

		return "redirect:/";
	}

	// Pagination and Sorting
	@GetMapping("/page/{pageNo}")
	public String findPaginated(@PathVariable(value = "pageNo") int pageNo, @RequestParam("sortField") String sortField,
			@RequestParam("sortDir") String sortDir, Model model) {
		int pageSize = 5;

		Page<Employee> page = employeeService.findPaginated(pageNo, pageSize, sortField, sortDir);
		List<Employee> listEmployees = page.getContent();

		// For pagination
		model.addAttribute("currentPage", pageNo);
		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());

		// For sorting
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

		model.addAttribute("listEmployees", listEmployees);

		return "index";
	}

	// Search
	private String search(String keyword, int pageNo, String sortField, String sortDir, Model model) {
		List<Employee> searchResults = employeeService.searchEmployees(keyword);
		model.addAttribute("listEmployees", searchResults);
		return "index";
	}

	
	// Filter
	private String filterByDepartmentOrPosition(String departmentFilter, String positionFilter, int pageNo, String sortField, String sortDir, Model model) {
	    List<Employee> filteredResults = employeeService.filterByDepartmentOrPosition(departmentFilter, positionFilter);
	    model.addAttribute("listEmployees", filteredResults);
	    return "index";
	}
}
