package com.Shakwa.utils.Validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.Shakwa.utils.exception.ObjectNotValidException;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ObjectsValidator<T> {
    private  final ValidatorFactory factory= Validation.buildDefaultValidatorFactory();
    @Autowired
    private final Validator validator=factory.getValidator();

    public void validate(T ObjectTOValidate)
    {
        Set<ConstraintViolation<T>> violation =validator.validate(ObjectTOValidate);
        if(!violation.isEmpty())
        {
            var errormessage=violation
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());
            throw new ObjectNotValidException(errormessage);
        }
    }
}
