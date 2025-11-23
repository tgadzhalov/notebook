package com.example.OnlineNotebook.models.dtos.admin;

import com.example.OnlineNotebook.models.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportUserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private UserType userType;
    private String studentClass;
}

