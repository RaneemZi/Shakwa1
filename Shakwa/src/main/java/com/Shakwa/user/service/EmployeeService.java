package com.Shakwa.user.service;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.dto.EmployeeCreateRequestDTO;
import com.Shakwa.user.dto.EmployeeResponseDTO;
import com.Shakwa.user.dto.EmployeeUpdateRequestDTO;
import com.Shakwa.user.entity.Employee;
import com.Shakwa.user.entity.Role;
import com.Shakwa.user.entity.User;
import com.Shakwa.user.mapper.EmployeeMapper;
import com.Shakwa.user.repository.EmployeeRepository;
import com.Shakwa.user.repository.CitizenRepo;
import com.Shakwa.user.repository.RoleRepository;
import com.Shakwa.user.repository.UserRepository;
import com.Shakwa.utils.exception.ResourceNotFoundException;
import com.Shakwa.utils.exception.UnAuthorizedException;

@Service
@Transactional
public class EmployeeService extends BaseSecurityService {
    
    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public EmployeeService(EmployeeRepository employeeRepository,
                          RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder,
                         UserRepository userRepository,
                         CitizenRepo citizenRepo) {
        super(userRepository, citizenRepo);
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    Logger logger = Logger.getLogger(EmployeeService.class.getName());
    
    @Transactional
    public EmployeeResponseDTO addEmployee(EmployeeCreateRequestDTO dto) {
        // السماح فقط لأدمن النظام بإنشاء موظفين
        User currentUser = getCurrentUser();
        if (!isAdmin()) {
            throw new UnAuthorizedException("Only platform admins can create employees");
        }
        
        // التحقق من وجود governmentAgency في الـ DTO
        if (dto.getGovernmentAgencyType() == null) {
            throw new UnAuthorizedException("Government agency type is required when creating an employee");
        }
        
        GovernmentAgencyType governmentAgency = dto.getGovernmentAgencyType();
        logger.info("Starting to add new employee: " + dto.getFirstName() + " " + dto.getLastName() + " for government agency: " + governmentAgency.getLabel());
        
        // Generate and validate email
        String email = generateEmployeeEmail(dto, governmentAgency);
        validateEmployeeEmail(email);
        
        // Create and save employee
        Employee employee = createEmployeeFromDTO(dto, email, governmentAgency);
        employee = saveEmployee(employee);
        
        logger.info("Employee creation completed successfully");
        return EmployeeMapper.toResponseDTO(employee);
    }
    
    public List<EmployeeResponseDTO> getAllEmployeesInGovernmentAgency() {
        // Validate that the current user is a governmentAgency manager
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only governmentAgency employees can access employee data");
        }
        
        Employee manager = (Employee) currentUser;
        if (manager.getGovernmentAgency() == null) {
            throw new UnAuthorizedException("Employee is not associated with any governmentAgency");
        }
        
        GovernmentAgencyType governmentAgency = manager.getGovernmentAgency();
        logger.info("Getting all employees for governmentAgency: " + governmentAgency.getLabel());
        return employeeRepository.findByGovernmentAgency(governmentAgency)
                .stream()
                .map(EmployeeMapper::toResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    public EmployeeResponseDTO updateEmployeeInGovernmentAgency(Long employeeId, EmployeeUpdateRequestDTO dto) {
        // Validate that the current user is a platform admin
        User currentUser = getCurrentUser();
        if (!isAdmin()) {
            throw new UnAuthorizedException("Only platform admins can update employees");
        }
        
        logger.info("Starting to update employee with ID: " + employeeId);
        
        // Validate and get employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        // Update employee fields using mapper (password is not updated for security)
        updateEmployeeFields(employee, dto);
        
        
        // Save and return
        employee = saveEmployee(employee);
        logger.info("Employee update completed successfully");
        return EmployeeMapper.toResponseDTO(employee);
    }
    @Transactional
    public void deleteEmployeeInGovernmentAgency(Long employeeId) {
        // Validate that the current user is a platform admin
        User currentUser = getCurrentUser();
        if (!isAdmin()) {
            throw new UnAuthorizedException("Only platform admins can delete employees");
        }
        
        logger.info("Starting to delete employee with ID: " + employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
       
        // Delete employee
        employeeRepository.delete(employee);
        logger.info("Employee deleted successfully");
    }
    
    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }
    
    /**
     * Get employee by ID with authorization check to ensure the current user has access to this employee
     * (same governmentAgency)
     * @param employeeId The ID of the employee to retrieve
     * @return EmployeeResponseDTO of the employee
     * @throws UnAuthorizedException if the current user doesn't have access to this employee
     */
    public EmployeeResponseDTO getEmployeeByIdWithAuth(Long employeeId) {
        // Get current user and validate they are an employee
        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only governmentAgency employees can access employee data");
        }
        
        Employee currentEmployee = (Employee) currentUser;
        if (currentEmployee.getGovernmentAgency() == null) {
            throw new UnAuthorizedException("Employee is not associated with any governmentAgency");
        }
        
        GovernmentAgencyType currentGovernmentAgency = currentEmployee.getGovernmentAgency();
        
        // Get the employee and validate governmentAgency access
        Employee employee = getEmployeeById(employeeId);
        if (!employee.getGovernmentAgency().equals(currentGovernmentAgency)) {
            throw new UnAuthorizedException("You can only access employees in your own governmentAgency");
        }
        
        return EmployeeMapper.toResponseDTO(employee);
    }

    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }
    
      
    // Helper methods for employee creation
    private String generateEmployeeEmail(EmployeeCreateRequestDTO dto, GovernmentAgencyType governmentAgency) {
        // Convert Arabic names to English transliteration for email generation
        String transliteratedFirstName = transliterateArabicToEnglish(dto.getFirstName());
        String transliteratedLastName = dto.getLastName() != null && !dto.getLastName().trim().isEmpty() ? 
            transliterateArabicToEnglish(dto.getLastName()) : "";
        
        // Clean transliterated names for email generation
        String cleanFirstName = transliteratedFirstName
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        
        String cleanLastName = transliteratedLastName
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        
        // Clean governmentAgency name for uniqueness
        String transliteratedGovernmentAgencyName = transliterateArabicToEnglish(governmentAgency.getLabel());
        String cleanGovernmentAgencyName = transliteratedGovernmentAgencyName
            .replaceAll("[^a-zA-Z0-9]", "")
            .toLowerCase();
        
        // Generate base email - combining transliterated names, governmentAgency name and license suffix for uniqueness
        String baseEmail;
        if (cleanLastName.isEmpty()) {
            baseEmail = cleanFirstName + "." + cleanGovernmentAgencyName + "@gov.com";
        } else {
            baseEmail = cleanFirstName + "." + cleanLastName + "." + cleanGovernmentAgencyName + "@gov.com";
        }
        
        logger.info("Generated base email for employee: " + baseEmail);
        
        // Ensure email uniqueness
        return ensureEmployeeEmailUniqueness(baseEmail);
    }
    
    private void validateEmployeeEmail(String email) {
        if(employeeRepository.findByEmail(email).isPresent()) {
            throw new ResourceNotFoundException("Employee with email: " + email + " already exists");
        }
    }
    
    /**
     * Transliterates Arabic text to English for email generation
     * @param arabicText The Arabic text to transliterate
     * @return English transliteration
     */
    private String transliterateArabicToEnglish(String arabicText) {
        if (arabicText == null || arabicText.trim().isEmpty()) {
            return "employee";
        }
        
        // Common Arabic words and their English equivalents
        String transliterated = arabicText
            // Common Arabic prefixes
            .replaceAll("ال", "al") // ال -> al (the)
            .replaceAll("بن", "bin") // بن -> bin (son of)
            .replaceAll("أبو", "abu") // أبو -> abu (father of)
            .replaceAll("أم", "um") // أم -> um (mother of)
            
            // Common Arabic letters transliteration
            .replaceAll("أ", "a")
            .replaceAll("إ", "i")
            .replaceAll("آ", "aa")
            .replaceAll("ع", "a")
            .replaceAll("غ", "gh")
            .replaceAll("ح", "h")
            .replaceAll("خ", "kh")
            .replaceAll("ق", "q")
            .replaceAll("ف", "f")
            .replaceAll("ث", "th")
            .replaceAll("ص", "s")
            .replaceAll("ض", "d")
            .replaceAll("ط", "t")
            .replaceAll("ك", "k")
            .replaceAll("م", "m")
            .replaceAll("ن", "n")
            .replaceAll("ه", "h")
            .replaceAll("و", "w")
            .replaceAll("ي", "y")
            .replaceAll("ة", "a")
            .replaceAll("ى", "a")
            
            // Remove remaining Arabic characters and special symbols
            .replaceAll("[\\u0600-\\u06FF]", "") // Remove all Arabic Unicode characters
            .replaceAll("[^a-zA-Z0-9\\s]", "") // Remove special characters except spaces
            .trim();
        
        // If transliteration resulted in empty string, use fallback
        if (transliterated.isEmpty() || transliterated.matches("\\s*")) {
            return "employee";
        }
        
        // Clean up multiple spaces and convert to single words
        transliterated = transliterated.replaceAll("\\s+", "");
        
        return transliterated;
    }
    
    /**
     * Ensures employee email uniqueness by appending numbers if needed
     * @param baseEmail The base email to check
     * @return Unique email address
     */
    private String ensureEmployeeEmailUniqueness(String baseEmail) {
        String email = baseEmail;
        int counter = 1;
        
        while (isEmployeeEmailExists(email)) {
            // Extract username and domain parts
            String[] parts = baseEmail.split("@");
            String username = parts[0];
            String domain = parts[1];
            
            // Append counter to username
            email = username + counter + "@" + domain;
            counter++;
            
            // Prevent infinite loop (safety check)
            if (counter > 1000) {
                logger.severe("Unable to generate unique employee email after 1000 attempts for base: " + baseEmail);
                throw new RuntimeException("Unable to generate unique employee email. Please contact support.");
            }
        }
        
        return email;
    }
    
    /**
     * Checks if an employee email already exists in the system
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    private boolean isEmployeeEmailExists(String email) {
        return employeeRepository.findByEmail(email).isPresent();
    }
    
    private Employee createEmployeeFromDTO(EmployeeCreateRequestDTO dto, String email, GovernmentAgencyType governmentAgency) {
        Employee employee = EmployeeMapper.toEntity(dto);
        employee.setEmail(email);
        employee.setGovernmentAgency(governmentAgency);
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        Role role = roleRepository.findById(dto.getRoleId()).orElseThrow(
                () -> new ResourceNotFoundException("Invalid role id: " + dto.getRoleId())
        );
        employee.setRole(role);
        
        return employee;
    }
    
    private Employee saveEmployee(Employee employee) {
        logger.info("Saving employee to database...");
        employee = employeeRepository.save(employee);
        logger.info("Employee saved with ID: " + employee.getId());
        return employee;
    }
    
       
    // Helper methods for employee update
    private Employee validateAndGetEmployee(Long employeeId, GovernmentAgencyType managerGovernmentAgency) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
        
        if (!employee.getGovernmentAgency().equals(managerGovernmentAgency)) {
            logger.warning("Manager tried to update employee " + employeeId + " from different governmentAgency");
            throw new AccessDeniedException("You can only update employees in your own governmentAgency");
        }
        
        return employee;
    }
    
    private void updateEmployeeFields(Employee employee, EmployeeUpdateRequestDTO dto) {
        logger.info("Updating employee fields...");
        
        // Use the mapper to update the entity
        EmployeeMapper.updateEntity(employee, dto);
        
        // Handle role update separately
        if(dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId()).orElseThrow(
                    () -> new ResourceNotFoundException("Invalid role id: " + dto.getRoleId())
            );
            employee.setRole(role);
        }
    }
    
   
} 