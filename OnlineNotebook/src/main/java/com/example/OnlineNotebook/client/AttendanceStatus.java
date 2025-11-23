package com.example.OnlineNotebook.client;

import lombok.Getter;

@Getter
public enum AttendanceStatus {
    ABSENT("Absent"),
    LATE("Late");

    private final String displayType;

    AttendanceStatus(String displayType) {
        this.displayType = displayType;
    }

}

