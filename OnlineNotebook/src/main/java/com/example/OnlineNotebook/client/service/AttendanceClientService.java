package com.example.OnlineNotebook.client.service;

import com.example.OnlineNotebook.client.AttendanceClient;
import com.example.OnlineNotebook.client.dto.AttendanceRequestDto;
import com.example.OnlineNotebook.client.dto.AttendanceResponseDto;
import com.example.OnlineNotebook.services.JwtTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AttendanceClientService {

    private final AttendanceClient client;
    private final JwtTokenService jwtTokenService;

    public AttendanceClientService(AttendanceClient client, JwtTokenService jwtTokenService) {
        this.client = client;
        this.jwtTokenService = jwtTokenService;
    }

    public ResponseEntity<AttendanceResponseDto> saveAttendance(UUID teacherId, AttendanceRequestDto attendanceRequestDto) {
        String token = jwtTokenService.generateToken(teacherId);
        String authorization = "Bearer " + token;
        return client.saveAttendance(authorization, attendanceRequestDto);
    }

    public ResponseEntity<List<AttendanceResponseDto>> getAttendances(UUID teacherId, UUID studentId) {
        String token = jwtTokenService.generateToken(teacherId);
        String authorization = "Bearer " + token;
        return client.getAttendances(authorization, studentId);
    }

    public ResponseEntity<Void> deleteAttendance(UUID teacherId, UUID attendanceId) {
        String token = jwtTokenService.generateToken(teacherId);
        String authorization = "Bearer " + token;
        return client.deleteAttendance(authorization, attendanceId);
    }
}


