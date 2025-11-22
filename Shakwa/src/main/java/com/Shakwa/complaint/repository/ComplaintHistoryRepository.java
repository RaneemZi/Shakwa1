package com.Shakwa.complaint.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Shakwa.complaint.entity.ComplaintHistory;

/**
 * Repository للوصول إلى سجل تغييرات الشكوى
 */
@Repository
public interface ComplaintHistoryRepository extends JpaRepository<ComplaintHistory, Long> {
    
    /**
     * البحث عن جميع السجلات لشكوى معينة مرتبة حسب التاريخ (الأحدث أولاً)
     */
    @Query("SELECT h FROM ComplaintHistory h WHERE h.complaint.id = :complaintId ORDER BY h.createdAt DESC")
    List<ComplaintHistory> findByComplaintIdOrderByCreatedAtDesc(@Param("complaintId") Long complaintId);
    
    /**
     * البحث عن جميع السجلات لشكوى معينة مع pagination
     */
    @Query("SELECT h FROM ComplaintHistory h WHERE h.complaint.id = :complaintId ORDER BY h.createdAt DESC")
    Page<ComplaintHistory> findByComplaintIdOrderByCreatedAtDesc(@Param("complaintId") Long complaintId, Pageable pageable);
    
    /**
     * عدد السجلات لشكوى معينة
     */
    @Query("SELECT COUNT(h) FROM ComplaintHistory h WHERE h.complaint.id = :complaintId")
    long countByComplaintId(@Param("complaintId") Long complaintId);
}

