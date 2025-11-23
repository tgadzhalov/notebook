package com.example.OnlineNotebook.models.enums;

import lombok.Getter;

@Getter
public enum AssignmentStatus {
    TURNED_IN("Turned in"),
    MISSED("Missed");

    private final String displayType;

    AssignmentStatus(String displayType) {
        this.displayType = displayType;
    }

}