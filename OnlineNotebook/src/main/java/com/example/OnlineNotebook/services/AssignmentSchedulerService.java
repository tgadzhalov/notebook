package com.example.OnlineNotebook.services;

import com.example.OnlineNotebook.models.entities.Assignment;
import com.example.OnlineNotebook.models.enums.AssignmentStatus;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentSchedulerService {
    
    private final AssignmentRepository assignmentRepository;
    
    public AssignmentSchedulerService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }
    
    @Scheduled(cron = "0 0 0 * * ?")
    @CacheEvict(value = {"assignments", "studentHome"}, allEntries = true)
    public void updateAssignmentStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Assignment> assignments = assignmentRepository.findAll();
        
        for (Assignment assignment : assignments) {
            if (assignment.getStatus() == null && assignment.getDueDate().isBefore(now)) {
                assignment.setStatus(AssignmentStatus.MISSED);
                assignmentRepository.save(assignment);
            }
        }
    }
    
    @Scheduled(fixedRate = 3600000)
    @CacheEvict(value = {"studentHome"}, allEntries = true)
    public void refreshStudentHomeCache() {
    }
}

