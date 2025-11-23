package com.example.OnlineNotebook.models.enums;

import lombok.Getter;

@Getter
public enum UserType {
    STUDENT("Student"),
    TEACHER("Teacher"),
    ADMIN("Admin");

    private final String displayType;

    UserType(String displayType) {
        this.displayType = displayType;
    }

}
