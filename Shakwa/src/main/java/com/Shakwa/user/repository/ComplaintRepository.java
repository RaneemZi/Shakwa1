package com.Shakwa.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Shakwa.user.Enum.ComplaintStatus;
import com.Shakwa.user.Enum.ComplaintType;
import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.Enum.Governorate;
import com.Shakwa.user.entity.Complaint;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // البحث عن الشكاوى حسب المواطن
    List<Complaint> findByCitizenId(Long citizenId);

    // البحث عن الشكاوى حسب الجهة الحكومية
    List<Complaint> findByGovernmentAgency(GovernmentAgencyType governmentAgency);

    // البحث عن الشكاوى حسب الحالة
    List<Complaint> findByStatus(ComplaintStatus status);

    // البحث عن الشكاوى حسب نوع الشكوى
    List<Complaint> findByComplaintType(ComplaintType complaintType);

    // البحث عن الشكاوى حسب المحافظة
    List<Complaint> findByGovernorate(Governorate governorate);

    // البحث عن الشكاوى حسب المواطن والجهة الحكومية
    List<Complaint> findByCitizenIdAndGovernmentAgency(Long citizenId, GovernmentAgencyType governmentAgency);

    // البحث عن الشكاوى حسب الجهة الحكومية والحالة
    List<Complaint> findByGovernmentAgencyAndStatus(GovernmentAgencyType governmentAgency, ComplaintStatus status);

    // البحث عن شكوى معينة حسب المواطن
    Optional<Complaint> findByIdAndCitizenId(Long id, Long citizenId);

    // البحث عن الشكاوى حسب المواطن والحالة
    List<Complaint> findByCitizenIdAndStatus(Long citizenId, ComplaintStatus status);

    // البحث عن الشكاوى حسب الجهة الحكومية ونوع الشكوى
    List<Complaint> findByGovernmentAgencyAndComplaintType(GovernmentAgencyType governmentAgency, ComplaintType complaintType);

    // البحث عن الشكاوى حسب الجهة الحكومية والمحافظة
    List<Complaint> findByGovernmentAgencyAndGovernorate(GovernmentAgencyType governmentAgency, Governorate governorate);
}

