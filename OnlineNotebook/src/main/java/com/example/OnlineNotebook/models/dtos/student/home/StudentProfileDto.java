package com.example.OnlineNotebook.models.dtos.student.home;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class StudentProfileDto {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String studentClass;
    private final String courseName;
    private final String profilePictureUrl;
    private final String initials;
    private final String displayId;
}

