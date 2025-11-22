package com.Shakwa.user.entity;

import com.Shakwa.user.Enum.GovernmentAgencyType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "employees")
public class Employee extends User {

    @Override
    protected String getSequenceName() {
        return "employees_id_seq";
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "government_agency")
    private GovernmentAgencyType governmentAgency;

    private String phoneNumber;

    private LocalDate dateOfHire;
}
