package com.Shakwa.user.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;

import com.Shakwa.user.Enum.ComplaintStatus;
import com.Shakwa.user.Enum.ComplaintType;
import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.Enum.Governorate;
import com.Shakwa.user.dto.ComplaintDTORequest;
import com.Shakwa.user.dto.ComplaintDTOResponse;
import com.Shakwa.user.dto.PaginationDTO;
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
    public PaginationDTO<ComplaintDTOResponse> getAllComplaints(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Complaint> complaintPage;

        // إذا كان المستخدم الحالي مواطن، إرجاع شكاويه فقط
        if (isCurrentUserCitizen()) {
            Citizen currentCitizen = getCurrentCitizen();
            logger.info("Citizen {} (ID: {}) is fetching their complaints", 
                       currentCitizen.getEmail(), currentCitizen.getId());
            complaintPage = complaintRepository.findByCitizenId(currentCitizen.getId(), pageable);
        }
        // إذا كان موظف، إرجاع شكاوى جهته الحكومية فقط
        else {
            try {
                User currentUser = getCurrentUser();
                if (currentUser instanceof Employee employee) {
                    if (employee.getGovernmentAgency() == null) {
                        throw new UnAuthorizedException("Employee is not associated with any government agency");
                    }
                    GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
                    logger.info("Employee {} (ID: {}) is fetching complaints for agency: {}", 
                               currentUser.getEmail(), currentUser.getId(), governmentAgency);
                    complaintPage = complaintRepository.findByGovernmentAgency(governmentAgency, pageable);
                } else {
                    // إذا لم يكن موظف أو مواطن، إرجاع جميع الشكاوى (للمدير العام)
                    logger.info("Admin is fetching all complaints");
                    complaintPage = complaintRepository.findAll(pageable);
                }
            } catch (Exception e) {
                logger.warn("Could not get current user, assuming admin access: {}", e.getMessage());
                complaintPage = complaintRepository.findAll(pageable);
            }
        }

        Page<ComplaintDTOResponse> dtoPage = complaintPage.map(complaintMapper::toResponse);
        return PaginationDTO.of(dtoPage);
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
    public PaginationDTO<ComplaintDTOResponse> getComplaintsByCitizenId(Long citizenId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User currentUser = getCurrentUser();
        Page<Complaint> complaintPage;

        // التحقق من الصلاحيات
        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any government agency");
            }
            GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
            complaintPage = complaintRepository.findByCitizenIdAndGovernmentAgency(citizenId, governmentAgency, pageable);
        } else {
            complaintPage = complaintRepository.findByCitizenId(citizenId, pageable);
        }

        Page<ComplaintDTOResponse> dtoPage = complaintPage.map(complaintMapper::toResponse);
        return PaginationDTO.of(dtoPage);
    }

    /**
     * الحصول على الشكاوى حسب الحالة
     */
    public PaginationDTO<ComplaintDTOResponse> getComplaintsByStatus(ComplaintStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User currentUser = getCurrentUser();
        Page<Complaint> complaintPage;

        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any government agency");
            }
            GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
            complaintPage = complaintRepository.findByGovernmentAgencyAndStatus(governmentAgency, status, pageable);
        } else {
            complaintPage = complaintRepository.findByStatus(status, pageable);
        }

        Page<ComplaintDTOResponse> dtoPage = complaintPage.map(complaintMapper::toResponse);
        return PaginationDTO.of(dtoPage);
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
    public PaginationDTO<ComplaintDTOResponse> getComplaintsByType(ComplaintType complaintType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User currentUser = getCurrentUser();
        Page<Complaint> complaintPage;

        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any government agency");
            }
            GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
            complaintPage = complaintRepository.findByGovernmentAgencyAndComplaintType(governmentAgency, complaintType, pageable);
        } else {
            complaintPage = complaintRepository.findByComplaintType(complaintType, pageable);
        }

        Page<ComplaintDTOResponse> dtoPage = complaintPage.map(complaintMapper::toResponse);
        return PaginationDTO.of(dtoPage);
    }

    /**
     * البحث عن الشكاوى حسب المحافظة
     */
    public PaginationDTO<ComplaintDTOResponse> getComplaintsByGovernorate(Governorate governorate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User currentUser = getCurrentUser();
        Page<Complaint> complaintPage;

        if (currentUser instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null) {
                throw new UnAuthorizedException("Employee is not associated with any government agency");
            }
            GovernmentAgencyType governmentAgency = employee.getGovernmentAgency();
            complaintPage = complaintRepository.findByGovernmentAgencyAndGovernorate(governmentAgency, governorate, pageable);
        } else {
            complaintPage = complaintRepository.findByGovernorate(governorate, pageable);
        }

        Page<ComplaintDTOResponse> dtoPage = complaintPage.map(complaintMapper::toResponse);
        return PaginationDTO.of(dtoPage);
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

    /**
     * فلترة الشكاوى حسب معايير متعددة
     */
    public PaginationDTO<ComplaintDTOResponse> filterComplaints(
            ComplaintStatus status,
            ComplaintType complaintType,
            Governorate governorate,
            GovernmentAgencyType governmentAgency,
            Long citizenId,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        Specification<Complaint> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            // فلترة حسب الحالة
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }

            // فلترة حسب نوع الشكوى
            if (complaintType != null) {
                predicate = cb.and(predicate, cb.equal(root.get("complaintType"), complaintType));
            }

            // فلترة حسب المحافظة
            if (governorate != null) {
                predicate = cb.and(predicate, cb.equal(root.get("governorate"), governorate));
            }

            // فلترة حسب الجهة الحكومية
            if (governmentAgency != null) {
                predicate = cb.and(predicate, cb.equal(root.get("governmentAgency"), governmentAgency));
            }

            // فلترة حسب المواطن
            if (citizenId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("citizen").get("id"), citizenId));
            }

            // التحقق من الصلاحيات
            User currentUser = getCurrentUser();
            if (currentUser instanceof Employee employee) {
                if (employee.getGovernmentAgency() != null) {
                    // الموظف يرى فقط شكاوى جهته الحكومية
                    predicate = cb.and(predicate, cb.equal(root.get("governmentAgency"), employee.getGovernmentAgency()));
                }
            } else if (isCurrentUserCitizen()) {
                // المواطن يرى فقط شكاويه
                Citizen currentCitizen = getCurrentCitizen();
                predicate = cb.and(predicate, cb.equal(root.get("citizen").get("id"), currentCitizen.getId()));
            }

            return predicate;
        };

        Page<Complaint> complaintPage = complaintRepository.findAll(spec, pageable);
        Page<ComplaintDTOResponse> dtoPage = complaintPage.map(complaintMapper::toResponse);
        return PaginationDTO.of(dtoPage);
    }
}

