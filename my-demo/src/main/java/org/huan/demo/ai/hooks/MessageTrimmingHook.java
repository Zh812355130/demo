package org.huan.demo.ai.hooks;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.messages.UpdatePolicy;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

@HookPositions({HookPosition.BEFORE_MODEL})
public class MessageTrimmingHook extends MessagesModelHook {
    private static final int MAX_MESSAGES = 10;
    @Override
    public AgentCommand beforeModel(List<Message> previousMessages, RunnableConfig config) {
        System.out.println("调用model之前执行");
        if(previousMessages.size() > MAX_MESSAGES){
            List<Message> trimmedMessages = previousMessages.subList(previousMessages.size() - MAX_MESSAGES, previousMessages.size());
            return new AgentCommand(trimmedMessages, UpdatePolicy.REPLACE);
        }
        return super.beforeModel(previousMessages, config);
    }

    @Override
    public String getName() {
        return "MessageTrimmingHook";
    }
}
