package com.example.OnlineNotebook.UnitTest.AssignmentSchedulerService;

import com.example.OnlineNotebook.models.entities.Assignment;
import com.example.OnlineNotebook.models.enums.AssignmentStatus;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import com.example.OnlineNotebook.services.AssignmentSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentSchedulerServiceUTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @InjectMocks
    private AssignmentSchedulerService assignmentSchedulerService;

    private LocalDateTime now;
    private LocalDateTime pastDate;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        pastDate = now.minusDays(1);
        futureDate = now.plusDays(1);
    }

    @Test
    void updateAssignmentStatuses_WhenAssignmentIsPastDueAndStatusIsNull_ShouldSetStatusToMissed() {
        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Test Assignment")
                .dueDate(pastDate)
                .status(null)
                .assignedDate(pastDate.minusDays(7))
                .build();

        List<Assignment> assignments = Collections.singletonList(assignment);
        when(assignmentRepository.findAll()).thenReturn(assignments);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);

        assignmentSchedulerService.updateAssignmentStatuses();

        ArgumentCaptor<Assignment> assignmentCaptor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(1)).save(assignmentCaptor.capture());
        assertEquals(AssignmentStatus.MISSED, assignmentCaptor.getValue().getStatus());
    }

    @Test
    void updateAssignmentStatuses_WhenAssignmentIsPastDueButStatusIsNotNull_ShouldNotUpdateStatus() {
        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Test Assignment")
                .dueDate(pastDate)
                .status(AssignmentStatus.TURNED_IN)
                .assignedDate(pastDate.minusDays(7))
                .build();

        List<Assignment> assignments = Collections.singletonList(assignment);
        when(assignmentRepository.findAll()).thenReturn(assignments);

        assignmentSchedulerService.updateAssignmentStatuses();

        verify(assignmentRepository, never()).save(any(Assignment.class));
        assertEquals(AssignmentStatus.TURNED_IN, assignment.getStatus());
    }

    @Test
    void updateAssignmentStatuses_WhenAssignmentIsNotPastDue_ShouldNotUpdateStatus() {
        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Test Assignment")
                .dueDate(futureDate)
                .status(null)
                .assignedDate(now)
                .build();

        List<Assignment> assignments = Collections.singletonList(assignment);
        when(assignmentRepository.findAll()).thenReturn(assignments);

        assignmentSchedulerService.updateAssignmentStatuses();

        verify(assignmentRepository, never()).save(any(Assignment.class));
        assertNull(assignment.getStatus());
    }

    @Test
    void updateAssignmentStatuses_WhenNoAssignmentsExist_ShouldNotSaveAnything() {
        when(assignmentRepository.findAll()).thenReturn(Collections.emptyList());

        assignmentSchedulerService.updateAssignmentStatuses();

        verify(assignmentRepository, never()).save(any(Assignment.class));
    }

    @Test
    void updateAssignmentStatuses_WhenMultipleAssignmentsWithDifferentScenarios_ShouldUpdateOnlyEligibleOnes() {
        Assignment pastDueNullStatus = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Past Due - Null Status")
                .dueDate(pastDate)
                .status(null)
                .assignedDate(pastDate.minusDays(7))
                .build();

        Assignment pastDueWithStatus = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Past Due - With Status")
                .dueDate(pastDate)
                .status(AssignmentStatus.TURNED_IN)
                .assignedDate(pastDate.minusDays(7))
                .build();

        Assignment futureDueNullStatus = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Future Due - Null Status")
                .dueDate(futureDate)
                .status(null)
                .assignedDate(now)
                .build();

        Assignment pastDueNullStatus2 = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Past Due - Null Status 2")
                .dueDate(pastDate.minusHours(5))
                .status(null)
                .assignedDate(pastDate.minusDays(7))
                .build();

        List<Assignment> assignments = Arrays.asList(
                pastDueNullStatus,
                pastDueWithStatus,
                futureDueNullStatus,
                pastDueNullStatus2
        );

        when(assignmentRepository.findAll()).thenReturn(assignments);
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assignmentSchedulerService.updateAssignmentStatuses();

        ArgumentCaptor<Assignment> assignmentCaptor = ArgumentCaptor.forClass(Assignment.class);
        verify(assignmentRepository, times(2)).save(assignmentCaptor.capture());

        List<Assignment> savedAssignments = assignmentCaptor.getAllValues();
        assertEquals(2, savedAssignments.size());
        assertTrue(savedAssignments.stream().allMatch(a -> a.getStatus() == AssignmentStatus.MISSED));
        assertTrue(savedAssignments.contains(pastDueNullStatus));
        assertTrue(savedAssignments.contains(pastDueNullStatus2));
    }

    @Test
    void updateAssignmentStatuses_WhenAssignmentDueDateIsExactlyNow_ShouldNotUpdateStatus() {
        LocalDateTime dueDate = now.plusSeconds(1);
        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Test Assignment")
                .dueDate(dueDate)
                .status(null)
                .assignedDate(dueDate.minusDays(7))
                .build();

        List<Assignment> assignments = Collections.singletonList(assignment);
        when(assignmentRepository.findAll()).thenReturn(assignments);

        assignmentSchedulerService.updateAssignmentStatuses();

        verify(assignmentRepository, never()).save(any(Assignment.class));
        assertNull(assignment.getStatus());
    }

    @Test
    void refreshStudentHomeCache_happyPath() {

        assertDoesNotThrow(() -> assignmentSchedulerService.refreshStudentHomeCache());
    }
}
