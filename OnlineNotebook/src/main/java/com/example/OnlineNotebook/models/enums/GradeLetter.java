package com.example.OnlineNotebook.models.enums;

import lombok.Getter;

@Getter
public enum GradeLetter {
    EXCELLENT("Excellent - 6"),
    VERY_GOOD("Very good - 5"),
    GOOD("Good - 4"),
    AVERAGE("Average - 3"),
    BAD("Bad - 2");

    private final String displayType;

    GradeLetter(String displayType) {
        this.displayType = displayType;
    }

}