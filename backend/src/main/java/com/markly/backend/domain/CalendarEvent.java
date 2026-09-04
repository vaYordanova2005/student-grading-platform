package com.markly.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "calendar_events")
@Getter
@Setter
@NoArgsConstructor
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CalendarEventType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    /**
     * Only meaningful when {@link #type} is {@link CalendarEventType#TEST} —
     * a holiday or a company visit isn't tied to a subject.
     */
    @Column
    private String subject;

    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * Null for single-day entries; set for ranges such as a week-long
     * holiday.
     */
    @Column
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public CalendarEvent(
            CalendarEventType type,
            String title,
            String description,
            String subject,
            LocalDate startDate,
            LocalDate endDate,
            User createdBy) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.subject = subject;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
