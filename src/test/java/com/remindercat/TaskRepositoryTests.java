package com.remindercat;

import com.remindercat.entity.Task;
import com.remindercat.entity.TaskStatus;
import com.remindercat.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskRepositoryTests {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldCreateQueryAndCompleteTask() {
        String userId = "repository-user-" + UUID.randomUUID();
        Task created = taskRepository.createTask(Task.builder()
                .userId(userId)
                .content("持久化提醒")
                .remindTime(LocalDateTime.of(2026, 8, 12, 9, 0))
                .status(TaskStatus.PENDING)
                .createdTime(LocalDateTime.now())
                .build());

        assertThat(created.getId()).isNotNull();
        assertThat(taskRepository.getTasksByUser(userId))
                .singleElement()
                .extracting(Task::getContent)
                .isEqualTo("持久化提醒");

        Task completed = taskRepository.completeTask(created.getId());
        assertThat(completed.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    void shouldAtomicallyClaimPendingTaskOnlyOnce() {
        Task created = taskRepository.createTask(Task.builder()
                .userId("claim-user-" + UUID.randomUUID())
                .content("抢占提醒")
                .remindTime(LocalDateTime.now().minusMinutes(1))
                .status(TaskStatus.PENDING)
                .createdTime(LocalDateTime.now())
                .build());

        assertThat(taskRepository.claimTask(created.getId())).isTrue();
        assertThat(taskRepository.claimTask(created.getId())).isFalse();
        assertThat(taskRepository.findById(created.getId()).getStatus())
                .isEqualTo(TaskStatus.PROCESSING);
    }
}
