package com.example.OnlineNotebook.models.enums;

import lombok.Getter;

@Getter
public enum AssignmentType {
    HOMEWORK("Homework"),
    TEST_PREPARATION("Test preparation"),
    OPTIONAL_WORK("Optional work");

    private final String displayType;

    AssignmentType(String displayType) {
        this.displayType = displayType;
    }

}