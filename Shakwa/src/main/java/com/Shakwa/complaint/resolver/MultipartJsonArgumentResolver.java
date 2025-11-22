package com.Shakwa.complaint.resolver;

import com.Shakwa.utils.exception.ObjectNotValidException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom argument resolver that automatically parses and validates JSON from multipart form data.
 * This allows controllers to directly receive DTOs from multipart "data" parts without manual parsing.
 * 
 * Usage: @RequestPart("data") ComplaintDTORequest dto
 * The resolver will automatically parse the JSON string and validate it using ObjectsValidator pattern.
 */
@Component
public class MultipartJsonArgumentResolver implements HandlerMethodArgumentResolver, Ordered {

    /**
     * Set high priority to ensure this resolver runs before Spring's default RequestPartArgumentResolver
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private final ObjectMapper objectMapper;
    private final ValidatorFactory validatorFactory;
    private final Validator validator;

    public MultipartJsonArgumentResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // Only support @RequestPart("data") parameters that are not String or MultipartFile
        boolean hasRequestPart = parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestPart.class);
        if (!hasRequestPart) {
            return false;
        }
        
        org.springframework.web.bind.annotation.RequestPart requestPart = 
            parameter.getParameterAnnotation(org.springframework.web.bind.annotation.RequestPart.class);
        
        // Only handle "data" parts
        if (requestPart == null || !"data".equals(requestPart.value())) {
            return false;
        }
        
        // Don't handle String or MultipartFile types (let Spring handle those normally)
        Class<?> paramType = parameter.getParameterType();
        return paramType != String.class && 
               paramType != org.springframework.web.multipart.MultipartFile.class &&
               !java.util.List.class.isAssignableFrom(paramType);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, 
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, 
                                  WebDataBinderFactory binderFactory) throws Exception {
        
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (servletRequest == null) {
            throw new IllegalStateException("Expected HTTP servlet request");
        }
        
        // Check if it's a multipart request
        if (!(servletRequest instanceof MultipartHttpServletRequest)) {
            throw new IllegalStateException("Expected multipart request");
        }
        
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) servletRequest;
        
        // Try multiple ways to get the "data" part
        String dataString = null;
        
        // Method 1: Try to get it as a MultipartFile
        MultipartFile dataFile = multipartRequest.getFile("data");
        if (dataFile != null && !dataFile.isEmpty()) {
            try {
                dataString = new String(dataFile.getBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to read 'data' part content: " + e.getMessage(), e);
            }
        }
        
        // Method 2: Try to get it as a parameter (for text form fields)
        if (dataString == null || dataString.trim().isEmpty()) {
            dataString = multipartRequest.getParameter("data");
        }
        
        // Method 3: Try to get it from Servlet Part directly
        if ((dataString == null || dataString.trim().isEmpty()) && servletRequest instanceof jakarta.servlet.MultipartConfigElement) {
            try {
                Collection<Part> parts = servletRequest.getParts();
                for (Part part : parts) {
                    if ("data".equals(part.getName())) {
                        dataString = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                        break;
                    }
                }
            } catch (Exception e) {
                // Ignore - try next method
            }
        }
        
        if (dataString == null || dataString.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing 'data' part in multipart request. Available parts: " + 
                multipartRequest.getFileMap().keySet());
        }

        // Parse JSON string to target DTO type
        Class<?> paramType = parameter.getParameterType();
        Object dto;
        try {
            dto = objectMapper.readValue(dataString, paramType);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid JSON format for 'data' part: " + e.getMessage(), e);
        }

        // Validate using ObjectsValidator pattern (validates based on annotations)
        // This matches the behavior of ObjectsValidator component
        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<Object>> violations = (Set<ConstraintViolation<Object>>) (Set<?>) validator.validate(dto);
        if (!violations.isEmpty()) {
            Set<String> errorMessages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());
            throw new ObjectNotValidException(errorMessages);
        }

        return dto;
    }
}

