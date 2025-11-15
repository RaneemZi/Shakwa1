package com.Shakwa.utils.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@MappedSuperclass
@NoArgsConstructor
@SuperBuilder
public abstract class BaseIdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
    @SequenceGenerator(
            name = "global_seq",
            sequenceName = "global_id_seq",
            allocationSize = 1
    )
    private Long id;
}
