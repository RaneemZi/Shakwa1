package com.Shakwa.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.Shakwa.user.Enum.ComplaintStatus;
import com.Shakwa.user.Enum.ComplaintType;
import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.Enum.Governorate;
import com.Shakwa.user.entity.Complaint;

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
}

