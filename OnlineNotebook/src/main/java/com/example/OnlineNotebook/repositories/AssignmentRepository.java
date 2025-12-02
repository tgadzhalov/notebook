package com.example.OnlineNotebook.repositories;

import com.example.OnlineNotebook.models.entities.Assignment;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    @Cacheable(value = "assignments", key = "#course.id")
    List<Assignment> findByCourse(Course course);
    List<Assignment> findByCreatedByOrderByAssignedDateDesc(User createdBy);
}
