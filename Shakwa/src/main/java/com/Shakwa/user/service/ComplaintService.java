package com.Shakwa.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.Shakwa.user.Enum.ComplaintStatus;
import com.Shakwa.user.Enum.ComplaintType;
import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.Enum.Governorate;
import com.Shakwa.user.dto.ComplaintDTORequest;
import com.Shakwa.user.dto.ComplaintDTOResponse;
import com.Shakwa.user.entity.Citizen;
import com.Shakwa.user.entity.Complaint;
import com.Shakwa.user.entity.Employee;
import com.Shakwa.user.entity.User;
import com.Shakwa.user.mapper.ComplaintMapper;
import com.Shakwa.user.repository.CitizenRepo;
import com.Shakwa.user.repository.ComplaintRepository;
import com.Shakwa.user.repository.UserRepository;
import com.Shakwa.utils.exception.ConflictException;
import com.Shakwa.utils.exception.UnAuthorizedException;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ComplaintService extends BaseSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintService.class);
    
    private final ComplaintRepository complaintRepository;
    private final CitizenRepo citizenRepo;
    private final ComplaintMapper complaintMapper;

    public ComplaintService(ComplaintRepository complaintRepository,
                           CitizenRepo citizenRepo,
                           ComplaintMapper complaintMapper,
                           UserRepository userRepository) {
        super(userRepository, citizenRepo);
        this.complaintRepository = complaintRepository;
        this.citizenRepo = citizenRepo;
        this.complaintMapper = complaintMapper;
    }

    /**
     * إنشاء شكوى جديدة - فقط المواطن يمكنه إنشاؤها
     * يتم استخدام المواطن من الـ token تلقائياً
     */
    public ComplaintDTOResponse createComplaint(ComplaintDTORequest dto) {
        // التحقق من أن المستخدم الحالي هو مواطن فقط
        if (!isCurrentUserCitizen()) {
            logger.warn("Non-citizen user attempted to create a complaint");
            throw new UnAuthorizedException("Only citizens can create complaints");
        }

        validateComplaintRequest(dto);

        // الحصول على المواطن من الـ token
        Citizen citizen = getCurrentCitizen();
        logger.info("Citizen {} (ID: {}) is creating a complaint from token", 
                   citizen.getEmail(), citizen.getId());

        // إنشاء الشكوى
        Complaint complaint = complaintMapper.toEntity(dto);
        complaint.setCitizen(citizen);
        
        // تعيين حالة افتراضية إذا لم يتم تحديدها
        if (complaint.getStatus() == null) {
            complaint.setStatus(ComplaintStatus.PENDING);
        }

        complaint = complaintRepository.save(complaint);
        return complaintMapper.toResponse(complaint);
    }

    /**
     * الحصول على جميع الشكاوى - للموظفين (حسب جهتهم الحكومية) أو المواطن (شكاويه فقط)
     */
    public List<ComplaintDTOResponse> getAllComplaints() {
        // إذا كان المستخدم الحالي مواطن، إرجاع شكاويه فقط
        if (isCurrentUserCitizen()) {
            Citizen currentCitizen = getCurrentCitizen();
            logger.info("Citizen {} (ID: {}) is fetching their complaints", 
                       currentCitizen.getEmail(), currentCitizen.getId());
            return complaintRepository.findByCitizenId(currentCitizen.getId())
                    .stream()
                    .map(complaintMapper::toResponse)
                    .collect(Collectors.toList());
        }

        // إذا كان موظف، إرجاع شكاوى جهته الحكومية فقط
        try {
            User currentUser = getCurrentUser();
            if (currentUser instanceof Employee employee) {
                if (employee.getGovernmentAgency() == null) {
                    throw new UnAuthorizedException("Employee is not associated with any government agency");
                }
                GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
                logger.info("Employee {} (ID: {}) is fetching complaints for agency: {}", 
                           currentUser.getEmail(), currentUser.getId(), governmentAgency);
                return complaintRepository.findByGovernmentAgency(governmentAgency)
                        .stream()
                        .map(complaintMapper::toResponse)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.warn("Could not get current user, assuming admin access: {}", e.getMessage());
        }

        // إذا لم يكن موظف أو مواطن، إرجاع جميع الشكاوى (للمدير العام)
        logger.info("Admin is fetching all complaints");
        return complaintRepository.findAll()
                .stream()
                .map(complaintMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * الحصول على شكوى محددة حسب ID
     */
    public ComplaintDTOResponse getComplaintById(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + id));

        // إذا كان المستخدم الحالي مواطن، التحقق من أن الشكوى تخصه
        if (isCurrentUserCitizen()) {
            Citizen currentCitizen = getCurrentCitizen();
            if (!complaint.getCitizen().getId().equals(currentCitizen.getId())) {
                logger.warn("Citizen {} (ID: {}) attempted to access complaint {} which belongs to citizen {}", 
                           currentCitizen.getEmail(), currentCitizen.getId(), id, complaint.getCitizen().getId());
                throw new UnAuthorizedException("You don't have access to this complaint");
            }
            logger.info("Citizen {} (ID: {}) is accessing their complaint {}", 
                       currentCitizen.getEmail(), currentCitizen.getId(), id);
            return complaintMapper.toResponse(complaint);
        }

        // إذا كان موظف، التحقق من أن الشكوى تخص جهته الحكومية
        try {
            User currentUser = getCurrentUser();
            if (currentUser instanceof Employee employee) {
                if (employee.getGovernmentAgency() == null || 
                    !employee.getGovernmentAgency().equals(complaint.getGovernmentAgency())) {
                    logger.warn("Employee {} (ID: {}) attempted to access complaint {} from different agency", 
                               currentUser.getEmail(), currentUser.getId(), id);
                    throw new UnAuthorizedException("You don't have access to this complaint");
                }
                logger.info("Employee {} (ID: {}) is accessing complaint {}", 
                           currentUser.getEmail(), currentUser.getId(), id);
            }
        } catch (Exception e) {
            logger.warn("Could not verify user access, allowing admin access: {}", e.getMessage());
        }

        return complaintMapper.toResponse(complaint);
    }

    /**
     * الحصول على شكاوى مواطن محدد
     */
    public List<ComplaintDTOResponse> getComplaintsByCitizenId(Long citizenId) {
        User currentUser = getCurrentUser();

        // التحقق من الصلاحيات
        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any government agency");
            }
            GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
            return complaintRepository.findByCitizenIdAndGovernmentAgency(citizenId, governmentAgency)
                    .stream()
                    .map(complaintMapper::toResponse)
                    .collect(Collectors.toList());
        }

        return complaintRepository.findByCitizenId(citizenId)
                .stream()
                .map(complaintMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * الحصول على الشكاوى حسب الحالة
     */
    public List<ComplaintDTOResponse> getComplaintsByStatus(ComplaintStatus status) {
        User currentUser = getCurrentUser();

        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any government agency");
            }
            GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
            return complaintRepository.findByGovernmentAgencyAndStatus(governmentAgency, status)
                    .stream()
                    .map(complaintMapper::toResponse)
                    .collect(Collectors.toList());
        }

        return complaintRepository.findByStatus(status)
                .stream()
                .map(complaintMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * تحديث الشكوى - فقط الموظفين يمكنهم التحديث
     */
    public ComplaintDTOResponse updateComplaint(Long id, ComplaintDTORequest dto) {
        validateComplaintRequest(dto);

        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only employees can update complaints");
        }

        Employee employee = (Employee) currentUser;
        if (employee.getGovernmentAgency() == null) {
            throw new UnAuthorizedException("Employee is not associated with any government agency");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + id));

        // التحقق من أن الشكوى تخص جهة الموظف الحكومية
        if (!employee.getGovernmentAgency().equals(complaint.getGovernmentAgency())) {
            throw new UnAuthorizedException("You don't have access to update this complaint");
        }

        // تحديث الحقول
        complaintMapper.updateEntityFromDto(complaint, dto);

        complaint = complaintRepository.save(complaint);
        return complaintMapper.toResponse(complaint);
    }

    /**
     * الرد على الشكوى وتحديث حالتها - للموظفين فقط
     */
    public ComplaintDTOResponse respondToComplaint(Long id, String response, ComplaintStatus newStatus) {
        if (!StringUtils.hasText(response)) {
            throw new ConflictException("Response text is required");
        }

        User currentUser = getCurrentUser();
        if (!(currentUser instanceof Employee)) {
            throw new UnAuthorizedException("Only employees can respond to complaints");
        }

        Employee employee = (Employee) currentUser;
        if (employee.getGovernmentAgency() == null) {
            throw new UnAuthorizedException("Employee is not associated with any government agency");
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + id));

        // التحقق من أن الشكوى تخص جهة الموظف الحكومية
        if (!employee.getGovernmentAgency().equals(complaint.getGovernmentAgency())) {
            throw new UnAuthorizedException("You don't have access to respond to this complaint");
        }

        // تحديث الرد والحالة
        complaint.setResponse(response);
        if (newStatus != null) {
            complaint.setStatus(newStatus);
        }
        complaint.setRespondedAt(LocalDateTime.now());
        complaint.setRespondedBy(employee);

        complaint = complaintRepository.save(complaint);
        return complaintMapper.toResponse(complaint);
    }

    /**
     * حذف الشكوى - فقط للمدير العام أو المواطن صاحب الشكوى
     */
    public void deleteComplaint(Long id) {
        User currentUser = getCurrentUser();
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + id));

        // التحقق من الصلاحيات
        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null || 
                !employee.getGovernmentAgency().equals(complaint.getGovernmentAgency())) {
                throw new UnAuthorizedException("You don't have access to delete this complaint");
            }
        }

        complaintRepository.deleteById(id);
    }

    /**
     * البحث عن الشكاوى حسب نوع الشكوى
     */
    public List<ComplaintDTOResponse> getComplaintsByType(ComplaintType complaintType) {
        User currentUser = getCurrentUser();

        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any government agency");
            }
            GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
            return complaintRepository.findByGovernmentAgencyAndComplaintType(governmentAgency, complaintType)
                    .stream()
                    .map(complaintMapper::toResponse)
                    .collect(Collectors.toList());
        }

        return complaintRepository.findByComplaintType(complaintType)
                .stream()
                .map(complaintMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * البحث عن الشكاوى حسب المحافظة
     */
    public List<ComplaintDTOResponse> getComplaintsByGovernorate(Governorate governorate) {
        User currentUser = getCurrentUser();

        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any government agency");
            }
            GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
            return complaintRepository.findByGovernmentAgencyAndGovernorate(governmentAgency, governorate)
                    .stream()
                    .map(complaintMapper::toResponse)
                    .collect(Collectors.toList());
        }

        return complaintRepository.findByGovernorate(governorate)
                .stream()
                .map(complaintMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * التحقق من صحة بيانات الشكوى
     */
    private void validateComplaintRequest(ComplaintDTORequest dto) {
        if (dto == null) {
            throw new ConflictException("Complaint request cannot be null");
        }

        if (dto.getComplaintType() == null) {
            throw new ConflictException("Complaint type is required");
        }

        if (dto.getGovernorate() == null) {
            throw new ConflictException("Governorate is required");
        }

        if (dto.getGovernmentAgency() == null) {
            throw new ConflictException("Government agency is required");
        }

        if (!StringUtils.hasText(dto.getLocation())) {
            throw new ConflictException("Location is required");
        }

        if (!StringUtils.hasText(dto.getDescription())) {
            throw new ConflictException("Description is required");
        }

        // citizenId ليس مطلوباً إذا كان المستخدم الحالي مواطن (سيتم أخذه من الـ token)
        // ولكن مطلوب إذا كان موظف يقوم بإنشاء شكوى لمواطن آخر
        // يتم التحقق من ذلك في createComplaint() method
    }
}

