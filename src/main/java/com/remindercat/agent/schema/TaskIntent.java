package com.remindercat.agent.schema;

import com.remindercat.agent.Intent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskIntent {

    private Intent intent;

    private String content;

    private LocalDateTime remindTime;
}
