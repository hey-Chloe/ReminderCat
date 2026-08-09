package com.remindercat.agent.tool;

import com.remindercat.agent.AgentResult;
import com.remindercat.agent.AgentState;
import com.remindercat.agent.Intent;

public interface AgentTool {

    boolean supports(Intent intent);

    AgentResult<?> execute(AgentState state);
}
