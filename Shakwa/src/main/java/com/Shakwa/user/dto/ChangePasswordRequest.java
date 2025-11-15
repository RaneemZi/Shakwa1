package com.Shakwa.user.dto;

import com.Shakwa.utils.annotation.ValidPassword;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@Schema
public class ChangePasswordRequest {

    private String currentPassword;
    @ValidPassword
    private String newPassword;
    @ValidPassword
    private String ConfirmPassword;
}
