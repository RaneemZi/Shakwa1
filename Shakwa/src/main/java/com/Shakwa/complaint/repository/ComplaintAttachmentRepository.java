package com.Shakwa.complaint.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Shakwa.complaint.entity.ComplaintAttachment;

public interface ComplaintAttachmentRepository extends JpaRepository<ComplaintAttachment, Long> {

    Optional<ComplaintAttachment> findByIdAndComplaintId(Long id, Long complaintId);
}


