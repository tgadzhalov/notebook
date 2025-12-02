package com.example.OnlineNotebook.UnitTest.AttendanceService;

import com.example.OnlineNotebook.client.service.AttendanceClientService;
import com.example.OnlineNotebook.services.AttendanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteAttendanceRecordTest {

    @InjectMocks
    private AttendanceService attendanceService;
    @Mock
    private AttendanceClientService clientAttendanceService;

    @Test
    void deleteAttendanceRecord_whenValidIds_thenShouldCallClientService() {
        UUID teacherId = UUID.randomUUID();
        UUID attendanceId = UUID.randomUUID();

        when(clientAttendanceService.deleteAttendance(teacherId, attendanceId))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> attendanceService.deleteAttendanceRecord(teacherId, attendanceId));

        verify(clientAttendanceService, times(1)).deleteAttendance(teacherId, attendanceId);
    }
}












