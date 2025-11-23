package com.example.OnlineNotebook.client.dto;

import com.example.OnlineNotebook.client.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceRequestDto {
    private UUID studentId;
    private String name;
    private String courseName;
    private AttendanceStatus status;
}
