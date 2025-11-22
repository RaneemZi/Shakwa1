package com.Shakwa.utils.exception;

import lombok.Data;

import java.util.Set;

@Data
public class ObjectNotValidException extends RuntimeException {

    private final Set<String> errormessage;

    public ObjectNotValidException(Set<String> errormessage) {
        this.errormessage = errormessage;
    }

}
