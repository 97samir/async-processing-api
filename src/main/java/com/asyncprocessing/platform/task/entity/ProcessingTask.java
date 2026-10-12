
package com.asyncprocessing.platform.task.entity;

import com.asyncprocessing.platform.task.enums.TaskStatus;
import com.asyncprocessing.platform.task.enums.TaskType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ProcessingTask(TaskType type, String description) {
        this.type = type;
        this.description = description;
        this.status = TaskStatus.PENDING;
    }

    public void markAsProcessing() {
        this.status = TaskStatus.PROCESSING;
        this.errorMessage = null;
    }

    public void complete(String result) {
        this.result = result;
        this.status = TaskStatus.COMPLETED;
        this.errorMessage = null;
    }

    public void fail(String errorMessage) {
        this.status = TaskStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
