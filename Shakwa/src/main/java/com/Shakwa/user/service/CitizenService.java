package com.Shakwa.user.service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.Shakwa.config.JwtService;
import com.Shakwa.config.RateLimiterConfig;
import com.Shakwa.user.dto.AuthenticationRequest;
import com.Shakwa.user.dto.CitizenDTORequest;
import com.Shakwa.user.dto.CitizenDTOResponse;
import com.Shakwa.user.dto.PaginationDTO;
import com.Shakwa.user.dto.UserAuthenticationResponse;
import com.Shakwa.user.entity.Citizen;
import com.Shakwa.user.entity.Employee;
import com.Shakwa.user.entity.OtpVerification;
import com.Shakwa.user.entity.User;
import com.Shakwa.user.mapper.CitizenMapper;
import com.Shakwa.user.repository.CitizenRepo;
import com.Shakwa.user.repository.OtpVerificationRepository;
import com.Shakwa.user.repository.UserRepository;
import com.Shakwa.utils.exception.ConflictException;
import com.Shakwa.utils.exception.RequestNotValidException;
import com.Shakwa.utils.exception.TooManyRequestException;
import com.Shakwa.utils.exception.UnAuthorizedException;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class CitizenService extends BaseSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(CitizenService.class);
    private final CitizenRepo citizenRepo;
    private final CitizenMapper citizenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RateLimiterConfig rateLimiterConfig;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final OtpVerificationRepository otpRepository;
    private static final SecureRandom random = new SecureRandom();

    public CitizenService(CitizenRepo citizenRepo, CitizenMapper citizenMapper, 
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         JwtService jwtService,
                         RateLimiterConfig rateLimiterConfig,
                         RateLimiterRegistry rateLimiterRegistry,
                         AuthenticationManager authenticationManager,
                         EmailService emailService,
                         OtpVerificationRepository otpRepository) {
        super(userRepository, citizenRepo);
        this.citizenRepo = citizenRepo;
        this.citizenMapper = citizenMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.rateLimiterConfig = rateLimiterConfig;
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.otpRepository = otpRepository;
    }

    public PaginationDTO<CitizenDTOResponse> getAllCitizens(int page, int size) {
        User currentUser = getCurrentUser();
        // السماح للأدمن والموظفين بالوصول
        if (!isAdmin() && !(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only platform admins and governmentAgency employees can access citizens");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Citizen> citizensPage = citizenRepo.findAll(pageable);
        Page<CitizenDTOResponse> dtoPage = citizensPage.map(citizenMapper::toResponse);
        return PaginationDTO.of(dtoPage);
    }
    
    public CitizenDTOResponse getCitizenById(Long id) {
        User currentUser = getCurrentUser();
        // السماح للأدمن والموظفين بالوصول
        if (!isAdmin() && !(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only platform admins and governmentAgency employees can access citizens");
        }
        
        Citizen citizen = citizenRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Citizen not found with ID: " + id));
        return citizenMapper.toResponse(citizen);
    }

    public CitizenDTOResponse getCitizenByName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new ConflictException("Citizen name cannot be empty");
        }
        
        User currentUser = getCurrentUser();
        // السماح للأدمن والموظفين بالوصول
        if (!isAdmin() && !(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only platform admins and governmentAgency employees can access citizens");
        }
        
        Citizen citizen = citizenRepo.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Citizen not found with name: " + name));
        return citizenMapper.toResponse(citizen);
    }

    public PaginationDTO<CitizenDTOResponse> searchCitizensByName(String name, int page, int size) {
        User currentUser = getCurrentUser();
        // السماح للأدمن والموظفين بالوصول
        if (!isAdmin() && !(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only platform admins and governmentAgency employees can access citizens");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Citizen> citizensPage;
        
        if (!StringUtils.hasText(name)) {
            citizensPage = citizenRepo.findAll(pageable);
        } else {
            citizensPage = citizenRepo.findByNameContainingIgnoreCase(name, pageable);
        }
        
        Page<CitizenDTOResponse> dtoPage = citizensPage.map(citizenMapper::toResponse);
        return PaginationDTO.of(dtoPage);
    }

    public CitizenDTOResponse createCitizen(CitizenDTORequest dto) {
        validateCitizenRequest(dto);
        
        User currentUser = getCurrentUser();
        // السماح للأدمن والموظفين بإنشاء المواطنين
        if (!isAdmin() && !(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only platform admins and governmentAgency employees can create citizens");
        }
        
        // إذا كان موظف، يجب أن يكون مرتبط بجهة حكومية
        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any governmentAgency");
            }
        }
        
        if (StringUtils.hasText(dto.getName()) && !"cash citizen".equals(dto.getName())) {
            citizenRepo.findByName(dto.getName())
                    .ifPresent(existingCitizen -> { 
                        throw new ConflictException("Citizen with name '" + dto.getName() + "' already exists");
                    });
        }
      
        
        Citizen citizen = citizenMapper.toEntity(dto);
        citizen = citizenRepo.save(citizen);
        return citizenMapper.toResponse(citizen);
    }

    public CitizenDTOResponse updateCitizen(Long id, CitizenDTORequest dto) {
        validateCitizenRequest(dto);
        
        User currentUser = getCurrentUser();
        // السماح للأدمن والموظفين بتحديث المواطنين
        if (!isAdmin() && !(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only platform admins and governmentAgency employees can update citizens");
        }
        
        // إذا كان موظف، يجب أن يكون مرتبط بجهة حكومية
        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any governmentAgency");
            }
        }
        
        Citizen citizen = citizenRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Citizen with ID " + id + " not found"));

        if (StringUtils.hasText(dto.getName()) && !dto.getName().equals(citizen.getFirstName() + " " + citizen.getLastName())) {
            citizenRepo.findByName(dto.getName())
                    .ifPresent(existingCitizen -> {
                        if (!existingCitizen.getId().equals(id)) {
                            throw new ConflictException("Citizen with name '" + dto.getName() + "' already exists");
                        }
                    });
        }

        citizenMapper.updateEntityFromDto(citizen, dto);
        citizen = citizenRepo.save(citizen);
        return citizenMapper.toResponse(citizen);
    }

    public void deleteCitizen(Long id) {
        User currentUser = getCurrentUser();
        // السماح للأدمن والموظفين بحذف المواطنين
        if (!isAdmin() && !(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only platform admins and governmentAgency employees can delete citizens");
        }
        
        // إذا كان موظف، يجب أن يكون مرتبط بجهة حكومية
        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any governmentAgency");
            }
        }
        
        // التحقق من وجود المواطن
        citizenRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Citizen with ID " + id + " not found"));

        
        citizenRepo.deleteById(id);
    }

    /**
     * تسجيل مواطن جديد (Register) - يرسل OTP للإيميل
     */
    public CitizenDTOResponse register(CitizenDTORequest dto) {
        validateCitizenRegistrationRequest(dto);

        // التحقق من عدم وجود email مكرر
        if (citizenRepo.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists: " + dto.getEmail());
        }

        // إنشاء المواطن (غير مفعّل حتى يتم التحقق من OTP)
        Citizen citizen = citizenMapper.toEntity(dto);
        citizen.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        citizen = citizenRepo.save(citizen);
        logger.info("Citizen registered successfully with ID: {} and email: {}", citizen.getId(), dto.getEmail());
        
        // إنشاء OTP
        String otpCode = generateOtp();
        logger.info("OTP code generated for email: {} - OTP: {}", dto.getEmail(), otpCode);
        
        // حفظ OTP في قاعدة البيانات
        saveOtpVerification(dto.getEmail(), otpCode);
        logger.info("OTP saved to database for email: {}", dto.getEmail());
        
        // إرسال OTP بالبريد الإلكتروني (أو طباعته في logs في وضع التطوير)
        emailService.sendOtpEmail(dto.getEmail(), otpCode);
        logger.info("OTP email process completed for email: {}", dto.getEmail());
        
        // إرجاع response مع OTP (خاصة في وضع التطوير)
        CitizenDTOResponse response = citizenMapper.toResponse(citizen);
        
    
        return response;
    }

    /**
     * التحقق من OTP وتفعيل الحساب
     */
    public void verifyOtp(String email, String otpCode) {
        Optional<OtpVerification> otpOptional = otpRepository.findByEmail(email);
        
        if (otpOptional.isEmpty()) {
            throw new RequestNotValidException("OTP not found. Please request a new OTP.");
        }
        
        OtpVerification otp = otpOptional.get();
        
        // التحقق من انتهاء الصلاحية
        if (otp.isExpired()) {
            otpRepository.delete(otp);
            throw new RequestNotValidException("OTP has expired. Please request a new OTP.");
        }
        
        // التحقق من عدد المحاولات
        if (otp.isMaxAttemptsReached()) {
            otpRepository.delete(otp);
            throw new RequestNotValidException("Maximum attempts reached. Please request a new OTP.");
        }
        
        // التحقق من صحة OTP
        if (!otp.getOtpCode().equals(otpCode)) {
            otp.incrementAttempts();
            otpRepository.save(otp);
            throw new RequestNotValidException("Invalid OTP code. Attempts remaining: " + (3 - otp.getAttempts()));
        }
        
        // تفعيل الحساب - وضع علامة التحقق وحذف OTP
        otp.setIsVerified(true);
        otpRepository.save(otp);
        
        // بعد التحقق الناجح، يمكن حذف OTP أو تركه للتنظيف التلقائي
        otpRepository.delete(otp);
    }

    /**
     * إعادة إرسال OTP
     */
    public void resendOtp(String email) {
        // التحقق من وجود المواطن
        citizenRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Citizen not found with email: " + email));
        
        // حذف OTP القديم إن وجد
        otpRepository.deleteByEmail(email);
        
        // إنشاء وإرسال OTP جديد
        String otpCode = generateOtp();
        saveOtpVerification(email, otpCode);
        emailService.sendOtpEmail(email, otpCode);
    }

    /**
     * إنشاء OTP عشوائي مكون من 6 أرقام
     */
    private String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * حفظ OTP في قاعدة البيانات
     */
    private void saveOtpVerification(String email, String otpCode) {
        // حذف OTP القديم إن وجد
        otpRepository.deleteByEmail(email);
        
        OtpVerification otp = new OtpVerification();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setIsVerified(false);
        otp.setAttempts(0);
        otpRepository.save(otp);
    }

    /**
     * تسجيل دخول المواطن (Login)
     */
    public UserAuthenticationResponse login(AuthenticationRequest request, HttpServletRequest httpServletRequest) {
        String userIp = httpServletRequest.getRemoteAddr();
        if (rateLimiterConfig.getBlockedIPs().contains(userIp)) {
            throw new TooManyRequestException("Too many login attempts. Please try again later.");
        }

        String rateLimiterKey = "citizenLoginRateLimiter-" + userIp;
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(rateLimiterKey);

        if (rateLimiter.acquirePermission()) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword(),
                            new HashSet<>()
                    ));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            var citizen = citizenRepo.findByEmail(request.getEmail()).orElseThrow(
                    () -> new RequestNotValidException("Citizen email not found")
            );

            // التحقق من أن الحساب مفعّل (تم التحقق من OTP)
            // إذا كان هناك OTP غير مفعّل، يعني الحساب لم يتم التحقق منه بعد
            Optional<OtpVerification> otpOptional = otpRepository.findByEmail(request.getEmail());
            if (otpOptional.isPresent()) {
                throw new RequestNotValidException("Please verify your email first. Check your email for OTP code.");
            }

            var jwtToken = jwtService.generateToken(citizen);
            
            UserAuthenticationResponse response = citizenToAuthResponse(citizen);
            response.setToken(jwtToken);

            return response;
        } else {
            rateLimiterConfig.blockIP(userIp);
            throw new TooManyRequestException("Too many login attempts, Please try again later.");
        }
    }

    /**
     * تحويل Citizen إلى UserAuthenticationResponse
     */
    private UserAuthenticationResponse citizenToAuthResponse(Citizen citizen) {
        UserAuthenticationResponse response = new UserAuthenticationResponse();
        response.setEmail(citizen.getEmail());
        response.setFirstName(citizen.getFirstName());
        response.setLastName(citizen.getLastName());
        response.setRole("CITIZEN");
        response.setIsActive(true);
        return response;
    }

    /**
     * التحقق من صحة بيانات التسجيل
     */
    private void validateCitizenRegistrationRequest(CitizenDTORequest dto) {
        if (dto == null) {
            throw new ConflictException("Citizen request cannot be null");
        }
        
        if (!StringUtils.hasText(dto.getName())) {
            throw new ConflictException("Name is required");
        }
        
        if (!StringUtils.hasText(dto.getEmail())) {
            throw new ConflictException("Email is required");
        }
        
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new ConflictException("Password is required");
        }
        
        if (dto.getPassword().length() < 6) {
            throw new ConflictException("Password must be at least 6 characters");
        }
        
       
        
        
    }

    private void validateCitizenRequest(CitizenDTORequest dto) {
        if (dto == null) {
            throw new ConflictException("Citizen request cannot be null");
        }
        
       
    }


   

} 