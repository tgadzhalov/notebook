package com.example.OnlineNotebook.client.dto;

import com.example.OnlineNotebook.client.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceResponseDto {
    private UUID id;
    private UUID studentId;
    private AttendanceStatus status;
    private UUID markedById;
    private String studentName;
    private String studentCourse;
    private LocalDateTime createdAt;
}
