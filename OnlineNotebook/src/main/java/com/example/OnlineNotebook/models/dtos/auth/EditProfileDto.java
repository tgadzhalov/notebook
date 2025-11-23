package com.example.OnlineNotebook.models.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditProfileDto {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 25, message = "First name must be between 2 and 25 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 25, message = "Last name must be between 2 and 25 characters")
    private String lastName;

    @Size(min = 8, max = 50, message = "Current password must be between 8 and 50 characters")
    private String currentPassword;

    @Size(min = 8, max = 50, message = "New password must be between 8 and 50 characters")
    private String newPassword;

    @Size(min = 8, max = 50, message = "Confirm password must be between 8 and 50 characters")
    private String confirmPassword;

    private String profilePictureUrl;
}

