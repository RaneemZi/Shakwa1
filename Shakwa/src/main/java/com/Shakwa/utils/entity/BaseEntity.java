package com.Shakwa.utils.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@MappedSuperclass
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEntity extends BaseIdEntity implements Serializable {

    protected String getSequenceName() {
        throw new IllegalStateException("getSequenceName() must be overridden by the entity class");
    }
}
