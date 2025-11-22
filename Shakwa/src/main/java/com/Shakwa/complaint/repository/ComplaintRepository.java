package com.Shakwa.complaint.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import com.Shakwa.complaint.Enum.ComplaintStatus;
import com.Shakwa.complaint.Enum.ComplaintType;
import com.Shakwa.complaint.Enum.Governorate;
import com.Shakwa.complaint.entity.Complaint;
import com.Shakwa.user.Enum.GovernmentAgencyType;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {

    // البحث عن الشكاوى حسب المواطن
    Page<Complaint> findByCitizenId(Long citizenId, Pageable pageable);

    // البحث عن الشكاوى حسب الجهة الحكومية
    Page<Complaint> findByGovernmentAgency(GovernmentAgencyType governmentAgency, Pageable pageable);

    // البحث عن الشكاوى حسب الحالة
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);

    // البحث عن الشكاوى حسب نوع الشكوى
    Page<Complaint> findByComplaintType(ComplaintType complaintType, Pageable pageable);

    // البحث عن الشكاوى حسب المحافظة
    Page<Complaint> findByGovernorate(Governorate governorate, Pageable pageable);

    // البحث عن الشكاوى حسب المواطن والجهة الحكومية
    Page<Complaint> findByCitizenIdAndGovernmentAgency(Long citizenId, GovernmentAgencyType governmentAgency, Pageable pageable);
    List<Complaint> findByCitizenIdAndGovernmentAgency(Long citizenId, GovernmentAgencyType governmentAgency);

    // البحث عن الشكاوى حسب الجهة الحكومية والحالة
    Page<Complaint> findByGovernmentAgencyAndStatus(GovernmentAgencyType governmentAgency, ComplaintStatus status, Pageable pageable);
    List<Complaint> findByGovernmentAgencyAndStatus(GovernmentAgencyType governmentAgency, ComplaintStatus status);

    // البحث عن شكوى معينة حسب المواطن
    Optional<Complaint> findByIdAndCitizenId(Long id, Long citizenId);

    // البحث عن الشكاوى حسب المواطن والحالة
    Page<Complaint> findByCitizenIdAndStatus(Long citizenId, ComplaintStatus status, Pageable pageable);

    // البحث عن الشكاوى حسب الجهة الحكومية ونوع الشكوى
    Page<Complaint> findByGovernmentAgencyAndComplaintType(GovernmentAgencyType governmentAgency, ComplaintType complaintType, Pageable pageable);
    List<Complaint> findByGovernmentAgencyAndComplaintType(GovernmentAgencyType governmentAgency, ComplaintType complaintType);

    // البحث عن الشكاوى حسب الجهة الحكومية والمحافظة
    Page<Complaint> findByGovernmentAgencyAndGovernorate(GovernmentAgencyType governmentAgency, Governorate governorate, Pageable pageable);
    List<Complaint> findByGovernmentAgencyAndGovernorate(GovernmentAgencyType governmentAgency, Governorate governorate);

    // Methods returning List for backward compatibility
    List<Complaint> findByCitizenId(Long citizenId);
    List<Complaint> findByGovernmentAgency(GovernmentAgencyType governmentAgency);
    List<Complaint> findByStatus(ComplaintStatus status);
    List<Complaint> findByComplaintType(ComplaintType complaintType);
    List<Complaint> findByGovernorate(Governorate governorate);

    boolean existsByTrackingNumber(String trackingNumber);

    /**
     * Find complaint by ID with pessimistic write lock (SELECT FOR UPDATE)
     * Used when employee opens complaint for editing to prevent concurrent modifications
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Complaint c WHERE c.id = :id")
    Optional<Complaint> findByIdForUpdate(@Param("id") Long id);

    /**
     * Find complaint by ID with pessimistic write lock and agency check
     * Used when employee opens complaint for editing within their agency
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Complaint c WHERE c.id = :id AND c.governmentAgency = :agency")
    Optional<Complaint> findByIdAndAgencyForUpdate(@Param("id") Long id, @Param("agency") GovernmentAgencyType agency);
}

