package com.Shakwa.complaint.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.Shakwa.complaint.entity.ComplaintHistory;
import com.Shakwa.complaint.repository.ComplaintHistoryRepository;
import com.Shakwa.user.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import org.apache.tika.Tika;

import jakarta.persistence.criteria.Predicate;

import com.Shakwa.complaint.Enum.ComplaintStatus;
import com.Shakwa.complaint.Enum.ComplaintType;
import com.Shakwa.complaint.Enum.Governorate;
import com.Shakwa.complaint.dto.ComplaintDTORequest;
import com.Shakwa.complaint.dto.ComplaintDTOResponse;
import com.Shakwa.complaint.dto.ComplaintHistoryDTO;
import com.Shakwa.complaint.entity.Complaint;
import com.Shakwa.complaint.entity.ComplaintAttachment;
import com.Shakwa.complaint.mapper.ComplaintHistoryMapper;
import com.Shakwa.complaint.mapper.ComplaintMapper;
import com.Shakwa.complaint.repository.ComplaintAttachmentRepository;
import com.Shakwa.complaint.repository.ComplaintRepository;
import com.Shakwa.complaint.storage.AttachmentStorageService;
import com.Shakwa.complaint.storage.AttachmentStorageService.StoredFile;
import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.dto.PaginationDTO;
import com.Shakwa.user.entity.Citizen;
import com.Shakwa.user.entity.Employee;
import com.Shakwa.user.entity.User;
import com.Shakwa.user.repository.CitizenRepo;
import com.Shakwa.user.repository.UserRepository;
import com.Shakwa.user.service.BaseSecurityService;
import com.Shakwa.utils.exception.ConflictException;
import com.Shakwa.utils.annotation.Audited;
import com.Shakwa.utils.exception.LockedException;
import com.Shakwa.utils.exception.OptimisticLockException;
import com.Shakwa.utils.exception.UnAuthorizedException;
import com.Shakwa.utils.response.FileDownloadResponse;


import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ComplaintService extends BaseSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintService.class);
    
    private final ComplaintRepository complaintRepository;
    private final CitizenRepo citizenRepo;
    private final ComplaintMapper complaintMapper;
    private final AttachmentStorageService attachmentStorageService;
    private final ComplaintAttachmentRepository complaintAttachmentRepository;
    private final TrackingNumberGenerator trackingNumberGenerator;
    private final ComplaintHistoryService complaintHistoryService;
    private final ComplaintHistoryMapper complaintHistoryMapper;
    private final ComplaintHistoryRepository complaintHistoryRepository;
    private final Tika tika = new Tika();

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png","image/jpg", "image/jpeg", "application/pdf");
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final long MAX_TOTAL_BYTES = 50 * 1024 * 1024; // 50 MB
    private static final int MAX_FILES_PER_OPERATION = 10;
    private final EmployeeRepository employeeRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            CitizenRepo citizenRepo,
                            ComplaintMapper complaintMapper,
                            UserRepository userRepository,
                            AttachmentStorageService attachmentStorageService,
                            ComplaintAttachmentRepository complaintAttachmentRepository,
                            TrackingNumberGenerator trackingNumberGenerator, 
                            EmployeeRepository employeeRepository,
                            ComplaintHistoryService complaintHistoryService,
                            ComplaintHistoryMapper complaintHistoryMapper,
                            ComplaintHistoryRepository complaintHistoryRepository) {
        super(userRepository, citizenRepo , employeeRepository);
        this.complaintRepository = complaintRepository;
        this.citizenRepo = citizenRepo;
        this.complaintMapper = complaintMapper;
        this.attachmentStorageService = attachmentStorageService;
        this.complaintAttachmentRepository = complaintAttachmentRepository;
        this.trackingNumberGenerator = trackingNumberGenerator;
        this.employeeRepository = employeeRepository;
        this.complaintHistoryService = complaintHistoryService;
        this.complaintHistoryMapper = complaintHistoryMapper;
        this.complaintHistoryRepository = complaintHistoryRepository;
    }

    /**
     * إنشاء شكوى جديدة - فقط المواطن يمكنه إنشاؤها
     * يتم استخدام المواطن من الـ token تلقائياً
     */
    @Audited(action = "CREATE_COMPLAINT", targetType = "COMPLAINT", includeArgs = false)
    public ComplaintDTOResponse createComplaint(ComplaintDTORequest dto, List<MultipartFile> files) {      
        // التحقق من أن المستخدم الحالي هو مواطن فقط
        if (!isCurrentUserCitizen()) {
            logger.warn("Non-citizen user attempted to create a complaint");
            throw new UnAuthorizedException("Only citizens can create complaints");
        }

        validateComplaintRequest(dto);

        // الحصول على المواطن من الـ token
        Citizen citizen = getCurrentCitizen();

        // إنشاء الشكوى
        Complaint complaint = complaintMapper.toEntity(dto);
        complaint.setCitizen(citizen);
        complaint.setTrackingNumber(generateTrackingNumber());
        
        // تعيين حالة افتراضية إذا لم يتم تحديدها
        if (complaint.getStatus() == null) {
            complaint.setStatus(ComplaintStatus.PENDING);
        }

        complaint = complaintRepository.save(complaint);

        // تسجيل إنشاء الشكوى في التاريخ
        complaintHistoryService.recordCreation(complaint, citizen);

        if (files != null && !files.isEmpty()) {
            storeAttachments(complaint, files, citizen);
            complaint = complaintRepository.save(complaint);
        }

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
                    complaintPage = complaintRepository.findByGovernmentAgency(governmentAgency, pageable);
                } else {
                    // إذا لم يكن موظف أو مواطن، إرجاع جميع الشكاوى (للمدير العام)
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
                throw new UnAuthorizedException("You don't have access to this complaint");
            }
            return complaintMapper.toResponse(complaint);
        }

        // إذا كان موظف، التحقق من أن الشكوى تخص جهته الحكومية
        try {
            User currentUser = getCurrentUser();
            if (currentUser instanceof Employee employee) {
                if (employee.getGovernmentAgency() == null || 
                    !employee.getGovernmentAgency().equals(complaint.getGovernmentAgency())) {
                    throw new UnAuthorizedException("You don't have access to this complaint");
                }
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
     * Uses state-based locking (IN_PROGRESS status) + pessimistic + optimistic locking
     */
    @Audited(action = "UPDATE_COMPLAINT", targetType = "COMPLAINT", includeArgs = false)
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

        // Use pessimistic locking (SELECT FOR UPDATE) to prevent concurrent edits
        Complaint complaint = complaintRepository.findByIdAndAgencyForUpdate(id, employee.getGovernmentAgency())
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + id));

        // State-based locking: Check if complaint is locked by another employee
        ensureNotLockedByState(complaint, employee, currentUser);

        // حفظ القيم القديمة لتسجيل التاريخ
        ComplaintStatus oldStatus = complaint.getStatus();
        String oldDescription = complaint.getDescription();
        String oldLocation = complaint.getLocation();
        
        try {
            // تحديث الحقول
            complaintMapper.updateEntityFromDto(complaint, dto);

            // Save will use optimistic locking (version check)
            complaint = complaintRepository.save(complaint);
            
            // تسجيل تغيير الحالة إذا تغيرت
            ComplaintStatus newStatus = complaint.getStatus();
            if (newStatus != null && !newStatus.equals(oldStatus)) {
                complaintHistoryService.recordStatusChange(complaint, currentUser, oldStatus, newStatus);
            }
            
            // تسجيل تحديث الحقول
            if (dto.getDescription() != null && !dto.getDescription().equals(oldDescription)) {
                complaintHistoryService.recordFieldUpdate(complaint, currentUser, "description", oldDescription, dto.getDescription());
            }
            if (dto.getLocation() != null && !dto.getLocation().equals(oldLocation)) {
                complaintHistoryService.recordFieldUpdate(complaint, currentUser, "location", oldLocation, dto.getLocation());
            }
        } catch (jakarta.persistence.OptimisticLockException e) {
            throw new OptimisticLockException(
                "تم تعديل هذه الشكوى من قبل موظف آخر. يرجى تحديث الصفحة والمحاولة مرة أخرى."
            );
        }
        
        return complaintMapper.toResponse(complaint);
    }

    /**
     * الرد على الشكوى وتحديث حالتها - للموظفين فقط
     * Uses state-based locking (IN_PROGRESS status) + pessimistic + optimistic locking
     */
    @Audited(action = "RESPOND_TO_COMPLAINT", targetType = "COMPLAINT", includeArgs = false)
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

        // Use pessimistic locking (SELECT FOR UPDATE) to prevent concurrent edits
        Complaint complaint = complaintRepository.findByIdAndAgencyForUpdate(id, employee.getGovernmentAgency())
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + id));

        // State-based locking: Check if complaint is locked by another employee
        // Only check if newStatus is IN_PROGRESS or complaint is already IN_PROGRESS
        if (newStatus == ComplaintStatus.IN_PROGRESS || complaint.getStatus() == ComplaintStatus.IN_PROGRESS) {
            ensureNotLockedByState(complaint, employee, currentUser);
        }

        // حفظ الحالة القديمة لتسجيل التاريخ
        ComplaintStatus oldStatus = complaint.getStatus();
        
        try {
            // تحديث الرد والحالة
            complaint.setResponse(response);
            if (newStatus != null) {
                complaint.setStatus(newStatus);
            }
            complaint.setRespondedAt(LocalDateTime.now());
            complaint.setRespondedBy(employee);

            // Save will use optimistic locking (version check)
            complaint = complaintRepository.save(complaint);
            
            // تسجيل تغيير الحالة إذا تغيرت
            if (newStatus != null && !newStatus.equals(oldStatus)) {
                complaintHistoryService.recordStatusChange(complaint, currentUser, oldStatus, newStatus);
                
                // تسجيل LOCKED/UNLOCKED بناءً على الحالة
                if (newStatus == ComplaintStatus.IN_PROGRESS) {
                    complaintHistoryService.recordLocked(complaint, currentUser);
                } else if (newStatus == ComplaintStatus.RESOLVED || 
                          newStatus == ComplaintStatus.REJECTED || 
                          newStatus == ComplaintStatus.CLOSED) {
                    complaintHistoryService.recordUnlocked(complaint, currentUser);
                }
            }
        } catch (jakarta.persistence.OptimisticLockException e) {
            throw new OptimisticLockException(
                "تم تعديل هذه الشكوى من قبل موظف آخر. يرجى تحديث الصفحة والمحاولة مرة أخرى."
            );
        }
        
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

        // حذف الملفات من التخزين الفيزيائي
        if (complaint.getAttachments() != null) {
            complaint.getAttachments().forEach(att -> attachmentStorageService.delete(att.getStoragePath()));
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

    public ComplaintDTOResponse addAttachments(Long complaintId, List<MultipartFile> files) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + complaintId));
        ensureCitizenOwnsComplaint(complaint);
        User currentUser = getCurrentUser();
        storeAttachments(complaint, files, currentUser);
        complaint = complaintRepository.save(complaint);
        return complaintMapper.toResponse(complaint);
    }

    public FileDownloadResponse downloadAttachment(Long complaintId, Long attachmentId) {
        ComplaintAttachment attachment = complaintAttachmentRepository.findByIdAndComplaintId(attachmentId, complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
        Complaint complaint = attachment.getComplaint();
        ensureCanAccessComplaint(complaint);
        Resource resource = attachmentStorageService.loadAsResource(attachment.getStoragePath());
        return new FileDownloadResponse(resource, attachment.getOriginalFilename(), attachment.getContentType(), attachment.getSize());
    }

    public void deleteAttachment(Long complaintId, Long attachmentId) {
        ComplaintAttachment attachment = complaintAttachmentRepository.findByIdAndComplaintId(attachmentId, complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found"));
        Complaint complaint = attachment.getComplaint();
        ensureCitizenOwnsComplaint(complaint);
        User currentUser = getCurrentUser();
        String fileName = attachment.getOriginalFilename();
        attachmentStorageService.delete(attachment.getStoragePath());
        complaint.getAttachments().remove(attachment);
        complaintAttachmentRepository.delete(attachment);
        
        // تسجيل حذف المرفق في التاريخ
        complaintHistoryService.recordAttachmentRemoved(complaint, currentUser, fileName);
    }

    private void ensureCitizenOwnsComplaint(Complaint complaint) {
        if (!isCurrentUserCitizen()) {
            throw new UnAuthorizedException("Only citizens can modify attachments on their complaints");
        }
        Citizen citizen = getCurrentCitizen();
        if (!complaint.getCitizen().getId().equals(citizen.getId())) {
            throw new UnAuthorizedException("You don't own this complaint");
        }
    }

    private void ensureCanAccessComplaint(Complaint complaint) {
        if (isCurrentUserCitizen()) {
            ensureCitizenOwnsComplaint(complaint);
            return;
        }
        User user = getCurrentUser();
        if (user instanceof Employee employee) {
            if (employee.getGovernmentAgency() == null ||
                !employee.getGovernmentAgency().equals(complaint.getGovernmentAgency())) {
                throw new UnAuthorizedException("You don't have access to this complaint");
            }
        }
    }

    private void storeAttachments(Complaint complaint, List<MultipartFile> files, User currentUser) {
        if (files == null || files.isEmpty()) {
            return;
        }
        validateFiles(files);
        if (complaint.getAttachments() == null) {
            complaint.setAttachments(new ArrayList<>());
        }
        for (MultipartFile file : files) {
            String detectedType = detectContentType(file);
            if (!ALLOWED_CONTENT_TYPES.contains(detectedType)) {
                throw new ConflictException("Unsupported file type: " + detectedType);
            }
            StoredFile stored = attachmentStorageService.store(file, complaint.getTrackingNumber());
            ComplaintAttachment attachment = new ComplaintAttachment();
            attachment.setComplaint(complaint);
            attachment.setOriginalFilename(file.getOriginalFilename());
            attachment.setStoredFilename(stored.storedFilename());
            attachment.setStoragePath(stored.relativePath());
            attachment.setContentType(detectedType);
            attachment.setSize(stored.size());
            attachment.setChecksum(stored.checksum());
            attachment.setUploadedBy(currentUser);
            attachment.setUploadedAt(LocalDateTime.now());
            complaint.getAttachments().add(attachment);
            
            // تسجيل إضافة المرفق في التاريخ
            complaintHistoryService.recordAttachmentAdded(complaint, currentUser, file.getOriginalFilename(), stored.relativePath());
        }
    }

    private void validateFiles(List<MultipartFile> files) {
        if (files.size() > MAX_FILES_PER_OPERATION) {
            throw new ConflictException("Cannot upload more than " + MAX_FILES_PER_OPERATION + " files at once");
        }
        long totalSize = 0;
        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                throw new ConflictException("File " + file.getOriginalFilename() + " exceeds maximum size of 10 MB");
            }
            totalSize += file.getSize();
        }
        if (totalSize > MAX_TOTAL_BYTES) {
            throw new ConflictException("Total attachments size exceeds 50 MB limit");
        }
    }

    private String detectContentType(MultipartFile file) {
        try {
            return tika.detect(file.getInputStream(), file.getOriginalFilename());
        } catch (Exception e) {
            throw new ConflictException("Unable to determine file type");
        }
    }

    private String generateTrackingNumber() {
        String tracking;
        int attempts = 0;
        do {
            tracking = trackingNumberGenerator.generate();
            attempts++;
            if (attempts > 5) {
                throw new IllegalStateException("Unable to generate unique tracking number");
            }
        } while (complaintRepository.existsByTrackingNumber(tracking));
        return tracking;
    }

    /**
     * State-based locking: Check if complaint is locked by another employee
     * A complaint is considered "locked" if:
     * - Status is IN_PROGRESS
     * - respondedBy is set to a different employee
     * 
     * Admins can always override the lock.
     */
    private void ensureNotLockedByState(Complaint complaint, Employee currentEmployee, User currentUser) {
        // If status is IN_PROGRESS and respondedBy is set
        if (complaint.getStatus() == ComplaintStatus.IN_PROGRESS && complaint.getRespondedBy() != null) {
            // Check if locked by another employee
            if (!complaint.getRespondedBy().getId().equals(currentEmployee.getId())) {
                // Admins can override
                if (!isAdmin(currentUser)) {
                    String lockOwnerName = complaint.getRespondedBy().getFirstName() + " " + 
                                         complaint.getRespondedBy().getLastName();
                    throw new LockedException(
                        String.format("الشكوى قيد المعالجة حالياً من قبل %s. لا يمكن تعديلها حتى يتم تغيير حالتها.", 
                            lockOwnerName)
                    );
                }
            }
        }
    }

    /**
     * الحصول على سجل تغييرات الشكوى
     * Citizens see only their own complaints, employees see their agency's complaints, admins see all
     */
    public PaginationDTO<ComplaintHistoryDTO> getComplaintHistory(Long complaintId, int page, int size) {
        // التحقق من الصلاحيات
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + complaintId));
        ensureCanAccessComplaint(complaint);
        
        // الحصول على التاريخ مع pagination
        Pageable pageable = PageRequest.of(page, size);
        Page<ComplaintHistory> historyPage = complaintHistoryRepository.findByComplaintIdOrderByCreatedAtDesc(complaintId, pageable);
        
        // تحويل إلى DTO
        Page<ComplaintHistoryDTO> dtoPage = historyPage.map(complaintHistoryMapper::toDTO);
        return PaginationDTO.of(dtoPage);
    }

    /**
     * Check if user is admin
     */
    private boolean isAdmin(User user) {
        return user.getRole() != null && 
               (user.getRole().getName().equalsIgnoreCase("ADMIN") || 
                user.getRole().getName().equalsIgnoreCase("PLATFORM_ADMIN") ||
                user.getRole().getName().equalsIgnoreCase("ROLE_ADMIN"));
    }
}

