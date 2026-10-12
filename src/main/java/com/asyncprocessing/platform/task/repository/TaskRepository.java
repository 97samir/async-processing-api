
package com.asyncprocessing.platform.task.repository;

import com.asyncprocessing.platform.task.entity.ProcessingTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository
        extends JpaRepository<ProcessingTask, Long> {
}
