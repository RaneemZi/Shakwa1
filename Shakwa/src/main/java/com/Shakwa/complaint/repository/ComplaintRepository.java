package com.Shakwa.complaint.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(value = "complaintLists", key = "'complaints:citizen:' + #citizenId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findByCitizenId(Long citizenId, Pageable pageable);

    // البحث عن الشكاوى حسب الجهة الحكومية
    @Cacheable(value = "complaintLists", key = "'complaints:agency:' + #governmentAgency.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findByGovernmentAgency(GovernmentAgencyType governmentAgency, Pageable pageable);

    // البحث عن الشكاوى حسب الحالة
    @Cacheable(value = "complaintLists", key = "'complaints:status:' + #status.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable pageable);

    // البحث عن الشكاوى حسب نوع الشكوى
    @Cacheable(value = "complaintLists", key = "'complaints:type:' + #complaintType.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findByComplaintType(ComplaintType complaintType, Pageable pageable);

    // البحث عن الشكاوى حسب المحافظة
    @Cacheable(value = "complaintLists", key = "'complaints:governorate:' + #governorate.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findByGovernorate(Governorate governorate, Pageable pageable);

    // البحث عن الشكاوى حسب المواطن والجهة الحكومية
    @Cacheable(value = "complaintLists", key = "'complaints:citizen:' + #citizenId + ':agency:' + #governmentAgency.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findByCitizenIdAndGovernmentAgency(Long citizenId, GovernmentAgencyType governmentAgency, Pageable pageable);
    @Cacheable(value = "complaintLists", key = "'complaints:citizen:' + #citizenId + ':agency:' + #governmentAgency.name()")
    List<Complaint> findByCitizenIdAndGovernmentAgency(Long citizenId, GovernmentAgencyType governmentAgency);

    // البحث عن الشكاوى حسب الجهة الحكومية والحالة
    @Cacheable(value = "complaintLists", key = "'complaints:agency:' + #governmentAgency.name() + ':status:' + #status.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findByGovernmentAgencyAndStatus(GovernmentAgencyType governmentAgency, ComplaintStatus status, Pageable pageable);
    @Cacheable(value = "complaintLists", key = "'complaints:agency:' + #governmentAgency.name() + ':status:' + #status.name()")
    List<Complaint> findByGovernmentAgencyAndStatus(GovernmentAgencyType governmentAgency, ComplaintStatus status);

    // البحث عن شكوى معينة حسب المواطن
    @Cacheable(value = "complaintLists", key = "'complaint:id:' + #id + ':citizen:' + #citizenId")
    Optional<Complaint> findByIdAndCitizenId(Long id, Long citizenId);

    // البحث عن الشكاوى حسب المواطن والحالة
    @Cacheable(value = "complaintLists", key = "'complaints:citizen:' + #citizenId + ':status:' + #status.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findByCitizenIdAndStatus(Long citizenId, ComplaintStatus status, Pageable pageable);

    // البحث عن الشكاوى حسب الجهة الحكومية ونوع الشكوى
    @Cacheable(value = "complaintLists", key = "'complaints:agency:' + #governmentAgency.name() + ':type:' + #complaintType.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findByGovernmentAgencyAndComplaintType(GovernmentAgencyType governmentAgency, ComplaintType complaintType, Pageable pageable);
    @Cacheable(value = "complaintLists", key = "'complaints:agency:' + #governmentAgency.name() + ':type:' + #complaintType.name()")
    List<Complaint> findByGovernmentAgencyAndComplaintType(GovernmentAgencyType governmentAgency, ComplaintType complaintType);

    // البحث عن الشكاوى حسب الجهة الحكومية والمحافظة
    @Cacheable(value = "complaintLists", key = "'complaints:agency:' + #governmentAgency.name() + ':governorate:' + #governorate.name() + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findByGovernmentAgencyAndGovernorate(GovernmentAgencyType governmentAgency, Governorate governorate, Pageable pageable);
    @Cacheable(value = "complaintLists", key = "'complaints:agency:' + #governmentAgency.name() + ':governorate:' + #governorate.name()")
    List<Complaint> findByGovernmentAgencyAndGovernorate(GovernmentAgencyType governmentAgency, Governorate governorate);

    // Methods returning List for backward compatibility
    @Cacheable(value = "complaintLists", key = "'complaints:citizen:' + #citizenId")
    List<Complaint> findByCitizenId(Long citizenId);
    @Cacheable(value = "complaintLists", key = "'complaints:agency:' + #governmentAgency.name()")
    List<Complaint> findByGovernmentAgency(GovernmentAgencyType governmentAgency);
    @Cacheable(value = "complaintLists", key = "'complaints:status:' + #status.name()")
    List<Complaint> findByStatus(ComplaintStatus status);
    @Cacheable(value = "complaintLists", key = "'complaints:type:' + #complaintType.name()")
    List<Complaint> findByComplaintType(ComplaintType complaintType);
    @Cacheable(value = "complaintLists", key = "'complaints:governorate:' + #governorate.name()")
    List<Complaint> findByGovernorate(Governorate governorate);

    boolean existsByTrackingNumber(String trackingNumber);

    /**
     * Find complaint by ID with pessimistic write lock (SELECT FOR UPDATE)
     * Used when employee opens complaint for editing to prevent concurrent modifications
     * Not cached as it's used for write operations
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Complaint c WHERE c.id = :id")
    Optional<Complaint> findByIdForUpdate(@Param("id") Long id);

    /**
     * Find complaint by ID with pessimistic write lock and agency check
     * Used when employee opens complaint for editing within their agency
     * Not cached as it's used for write operations
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Complaint c WHERE c.id = :id AND c.governmentAgency = :agency")
    Optional<Complaint> findByIdAndAgencyForUpdate(@Param("id") Long id, @Param("agency") GovernmentAgencyType agency);

    /**
     * Override save method to evict caches when saving
     */
    @Override
    @CacheEvict(value = "complaintLists", allEntries = true)
    <S extends Complaint> S save(S entity);

    /**
     * Override saveAll method to evict caches when saving
     */
    @Override
    @CacheEvict(value = "complaintLists", allEntries = true)
    <S extends Complaint> List<S> saveAll(Iterable<S> entities);

    /**
     * Override deleteById method to evict caches when deleting
     */
    @Override
    @CacheEvict(value = "complaintLists", allEntries = true)
    void deleteById(Long id);

    /**
     * Override delete method to evict caches when deleting
     */
    @Override
    @CacheEvict(value = "complaintLists", allEntries = true)
    void delete(Complaint entity);

    /**
     * Override deleteAll method to evict caches when deleting
     */
    @Override
    @CacheEvict(value = "complaintLists", allEntries = true)
    void deleteAll(Iterable<? extends Complaint> entities);

    /**
     * Override deleteAll method to evict caches when deleting
     */
    @Override
    @CacheEvict(value = "complaintLists", allEntries = true)
    void deleteAll();

    /**
     * Cache individual complaint by ID
     */
    @Override
    @Cacheable(value = "complaintLists", key = "'complaint:id:' + #id")
    Optional<Complaint> findById(Long id);

    /**
     * Cache findAll results with pagination
     */
    @Override
    @Cacheable(value = "complaintLists", key = "'complaints:all:page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    Page<Complaint> findAll(Pageable pageable);
}

