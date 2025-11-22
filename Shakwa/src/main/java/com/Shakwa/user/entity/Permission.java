package com.Shakwa.user.entity;

import com.Shakwa.utils.entity.BaseEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "permissions")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "permission_seq", sequenceName = "permissions_id_seq", allocationSize = 1)
public class Permission extends BaseEntity {
    
    @Override
    protected String getSequenceName() {
        return "permissions_id_seq";
    }
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private String resource; // e.g., "LEAD", "CONTACT", "DEALER"
    
    @Column(nullable = false)
    private String action; // e.g., "CREATE", "READ", "UPDATE", "DELETE"
    
    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private boolean isSystemGenerated;

} 