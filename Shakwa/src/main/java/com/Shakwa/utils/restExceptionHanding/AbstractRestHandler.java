package com.Shakwa.utils.restExceptionHanding;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.Shakwa.utils.exception.*;

import java.time.LocalDateTime;

@ControllerAdvice
public class AbstractRestHandler {

    @ExceptionHandler(value = {RequestNotValidException.class})
    public ResponseEntity<Object> handleRequestNotValidException(RequestNotValidException e){
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ApiException apiException = new ApiException(
                e.getMessage(),
                badRequest,
                LocalDateTime.now());
        return new ResponseEntity<>(apiException,badRequest);
    }
    @ExceptionHandler(value = {ObjectNotValidException.class})
    public ResponseEntity<Object> handleObjectNotValidException(ObjectNotValidException e){
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ApiException apiException = new ApiException(
                e.getErrormessage().toString(),
                badRequest,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException,badRequest);
    }

    @ExceptionHandler(value = {TooManyRequestException.class})
    public ResponseEntity<Object> handleTooManyRequestException(TooManyRequestException e){
        HttpStatus tooManyRequests = HttpStatus.TOO_MANY_REQUESTS;
        ApiException apiException = new ApiException(
                e.getMessage(),
                tooManyRequests,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException,tooManyRequests);
    }

    @ExceptionHandler(value = {UnAuthorizedException.class})
    public ResponseEntity<Object> handleUnauthorizedException(UnAuthorizedException e){
        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
        ApiException apiException = new ApiException(
                e.getMessage(),
                unauthorized,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException,unauthorized);
    }

    @ExceptionHandler(value = {BadCredentialsException.class})
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException e){
        HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
        ApiException apiException = new ApiException(
                e.getMessage(),
                unauthorized,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException,unauthorized);
    }

    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException e){
        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ApiException apiException = new ApiException(
                e.getMessage(),
                notFound,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException,notFound);
    }

    @ExceptionHandler(value = {ConflictException.class})
    public ResponseEntity<Object> handleConflictException(ConflictException e){
        HttpStatus conflict = HttpStatus.CONFLICT;
        ApiException apiException = new ApiException(
                e.getMessage(),
                conflict,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException,conflict);
    }

    @ExceptionHandler(value = {LockedException.class})
    public ResponseEntity<Object> handleLockedException(LockedException e){
        HttpStatus locked = HttpStatus.LOCKED; // 423
        ApiException apiException = new ApiException(
                e.getMessage(),
                locked,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException, locked);
    }

    @ExceptionHandler(value = {OptimisticLockException.class})
    public ResponseEntity<Object> handleOptimisticLockException(OptimisticLockException e){
        HttpStatus conflict = HttpStatus.CONFLICT; // 409 - Conflict due to concurrent modification
        ApiException apiException = new ApiException(
                e.getMessage(),
                conflict,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException, conflict);
    }

    @ExceptionHandler(value = {jakarta.persistence.OptimisticLockException.class})
    public ResponseEntity<Object> handleJpaOptimisticLockException(jakarta.persistence.OptimisticLockException e){
        HttpStatus conflict = HttpStatus.CONFLICT;
        ApiException apiException = new ApiException(
                "تم تعديل هذه الشكوى من قبل موظف آخر. يرجى تحديث الصفحة والمحاولة مرة أخرى.",
                conflict,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException, conflict);
    }

    @ExceptionHandler(value = {InvalidStatusTransitionException.class})
    public ResponseEntity<Object> handleInvalidStatusTransitionException(InvalidStatusTransitionException e){
        HttpStatus invalidStatus = HttpStatus.BAD_REQUEST;
        ApiException apiException = new ApiException(
                e.getMessage(),
                invalidStatus,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException,invalidStatus);
    }

    @ExceptionHandler(value = {EntityNotFoundException.class})
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException e){
        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ApiException apiException = new ApiException(
                e.getMessage(),
                notFound,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException,notFound);
    }

    @ExceptionHandler(value = {HttpMessageConversionException.class})
    public ResponseEntity<Object> handleHttpMessageConversionException(HttpMessageConversionException e){
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ApiException apiException = new ApiException(
                e.getMessage(),
                badRequest,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException,badRequest);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<?> handleGlobalException(Exception e){
        HttpStatus globalException = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiException apiException = new ApiException(
                e.getMessage(),
                globalException,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiException,globalException);
    }
}
