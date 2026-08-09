package com.remindercat.agent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentState {

    private String userId;

    private String input;

    private LocalDateTime remindTime;

    private Intent intent;
}
