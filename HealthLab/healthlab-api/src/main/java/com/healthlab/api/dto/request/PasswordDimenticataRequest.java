package com.healthlab.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordDimenticataRequest {
    @NotBlank
    private String email;
}