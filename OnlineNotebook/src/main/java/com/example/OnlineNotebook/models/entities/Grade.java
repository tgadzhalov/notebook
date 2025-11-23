package com.example.OnlineNotebook.models.entities;

import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.models.enums.GradeType;
import com.example.OnlineNotebook.models.enums.SubjectType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private User student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectType subjectType;

    @Enumerated(EnumType.STRING)
    private GradeLetter gradeLetter;

    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradeType gradeType;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private User gradedBy;

    @Column(nullable = false)
    private LocalDateTime dateGraded;
}