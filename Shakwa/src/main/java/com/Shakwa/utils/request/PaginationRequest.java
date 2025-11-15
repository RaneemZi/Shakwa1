package com.Shakwa.utils.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginationRequest {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 2;
    private static final int MAX_SIZE = 100;

    @Min(value = 1, message = "Page number should be greater than or equal to 1")
    private Integer page = DEFAULT_PAGE;

    @Min(value = 1, message = "Page size should be greater than or equal to 1")
    @Max(value = MAX_SIZE, message = "Page size should be less than or equal to " + MAX_SIZE + ".")
    private Integer size = DEFAULT_SIZE;
}