package com.example.OnlineNotebook.models.entities;

import com.example.OnlineNotebook.models.enums.AssignmentStatus;
import com.example.OnlineNotebook.models.enums.AssignmentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private AssignmentType type;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private LocalDateTime assignedDate;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private User createdBy;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Course course;
}