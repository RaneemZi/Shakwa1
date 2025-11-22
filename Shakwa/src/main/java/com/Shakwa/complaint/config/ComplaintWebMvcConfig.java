package com.Shakwa.complaint.config;

import com.Shakwa.complaint.converter.StringToComplaintStatusConverter;
import com.Shakwa.complaint.resolver.MultipartJsonArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuration to register custom argument resolvers and converters for complaint-related endpoints.
 */
@Configuration
public class ComplaintWebMvcConfig implements WebMvcConfigurer {

    private final MultipartJsonArgumentResolver multipartJsonArgumentResolver;
    private final StringToComplaintStatusConverter stringToComplaintStatusConverter;

    public ComplaintWebMvcConfig(
            MultipartJsonArgumentResolver multipartJsonArgumentResolver,
            StringToComplaintStatusConverter stringToComplaintStatusConverter) {
        this.multipartJsonArgumentResolver = multipartJsonArgumentResolver;
        this.stringToComplaintStatusConverter = stringToComplaintStatusConverter;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        // Remove our resolver if it's already in the list (to avoid duplicates)
        resolvers.removeIf(resolver -> resolver instanceof com.Shakwa.complaint.resolver.MultipartJsonArgumentResolver);
        // Add our custom resolver at the beginning to ensure it runs before Spring's default RequestPartArgumentResolver
        // This MUST be first so it intercepts before Spring tries to deserialize with HttpMessageConverters
        resolvers.add(0, multipartJsonArgumentResolver);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Register the converter to handle String to ComplaintStatus conversion
        // This allows request parameters like "IN PROGRESS" to be converted to ComplaintStatus.IN_PROGRESS
        registry.addConverter(stringToComplaintStatusConverter);
    }
}

