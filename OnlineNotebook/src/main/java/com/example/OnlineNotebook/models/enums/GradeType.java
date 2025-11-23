package com.example.OnlineNotebook.models.enums;

import lombok.Getter;

@Getter
public enum GradeType {
    ENTRY_EXAM("Entry exam"),
    END_OF_YEAR_EXAM("End of year exam"),
    MID_EXAM("Mid exam"),
    TEST("Test"),
    PROJECT("Project"),
    MID_TERM_NOTE("Mid term note"),
    END_OF_YEAR_NOTE("End of year note");

    private final String displayType;

    GradeType(String displayType) {
        this.displayType = displayType;
    }

}
