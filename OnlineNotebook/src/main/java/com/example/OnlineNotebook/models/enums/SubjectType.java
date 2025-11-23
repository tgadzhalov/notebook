package com.example.OnlineNotebook.models.enums;

import lombok.Getter;

@Getter
public enum SubjectType {
    MATH("Math"),
    ENGLISH("English"),
    BULGARIAN("Bulgarian"),
    SPORT("Sport"),
    SCIENCE("Science");

    private final String displayType;

    SubjectType(String displayType) {
        this.displayType = displayType;
    }

}