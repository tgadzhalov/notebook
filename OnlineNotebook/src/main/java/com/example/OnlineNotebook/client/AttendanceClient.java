package com.example.OnlineNotebook.client;

import com.example.OnlineNotebook.client.dto.AttendanceRequestDto;
import com.example.OnlineNotebook.client.dto.AttendanceResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "attendance-svc", url = "${attendance.microservice.base-url}/api/v1")
public interface AttendanceClient {

    @PostMapping("/attendance")
    ResponseEntity<AttendanceResponseDto> saveAttendance(
            @RequestHeader("Authorization") String authorization,
            @RequestBody AttendanceRequestDto attendanceRequestDto);

    @GetMapping("/attendance/student/{studentId}")
    ResponseEntity<List<AttendanceResponseDto>> getAttendances(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID studentId);

    @DeleteMapping("/attendance/{attendanceId}")
    ResponseEntity<Void> deleteAttendance(
            @RequestHeader("Authorization") String authorization,
            @PathVariable UUID attendanceId);
}
