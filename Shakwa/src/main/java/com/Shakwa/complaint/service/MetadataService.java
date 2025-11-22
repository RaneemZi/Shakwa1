package com.Shakwa.complaint.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.Shakwa.complaint.Enum.ComplaintType;
import com.Shakwa.complaint.Enum.Governorate;
import com.Shakwa.user.Enum.GovernmentAgencyType;

/**
 * Service for providing reference data (metadata) such as enum values.
 * All methods are cached indefinitely as reference data rarely changes.
 */
@Service
public class MetadataService {

    /**
     * Get all complaint types.
     * Cached indefinitely as reference data.
     */
    @Cacheable(value = "referenceData", key = "'complaintTypes'")
    public List<ComplaintType> getAllComplaintTypes() {
        return Arrays.asList(ComplaintType.values());
    }

    /**
     * Get all governorates.
     * Cached indefinitely as reference data.
     */
    @Cacheable(value = "referenceData", key = "'governorates'")
    public List<Governorate> getAllGovernorates() {
        return Arrays.asList(Governorate.values());
    }

    /**
     * Get all government agency types.
     * Cached indefinitely as reference data.
     */
    @Cacheable(value = "referenceData", key = "'governmentAgencies'")
    public List<GovernmentAgencyType> getAllGovernmentAgencies() {
        return Arrays.asList(GovernmentAgencyType.values());
    }

    /**
     * Get complaint types as string labels (for display).
     * Cached indefinitely as reference data.
     */
    @Cacheable(value = "referenceData", key = "'complaintTypeLabels'")
    public List<String> getComplaintTypeLabels() {
        return Arrays.stream(ComplaintType.values())
                .map(ComplaintType::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * Get governorates as string labels (for display).
     * Cached indefinitely as reference data.
     */
    @Cacheable(value = "referenceData", key = "'governorateLabels'")
    public List<String> getGovernorateLabels() {
        return Arrays.stream(Governorate.values())
                .map(Governorate::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * Get government agencies as string labels (for display).
     * Cached indefinitely as reference data.
     */
    @Cacheable(value = "referenceData", key = "'governmentAgencyLabels'")
    public List<String> getGovernmentAgencyLabels() {
        return Arrays.stream(GovernmentAgencyType.values())
                .map(GovernmentAgencyType::getLabel)
                .collect(Collectors.toList());
    }

    /**
     * Manually evict all reference data caches.
     * Use this method when reference data needs to be refreshed.
     */
    @CacheEvict(value = "referenceData", allEntries = true)
    public void evictReferenceDataCache() {
        // Method exists solely for cache eviction via annotation
    }
}

