package org.huan.demo.ai.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.List;

public class StateAwarePromptInterceptor extends ModelInterceptor {

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        List<Message> messages = request.getMessages();
        int messageCount = messages.size();

        String basePrompt = "你是一个有用的助手。";
        if (messageCount > 10) {
            basePrompt += "\n\n这是一个长对话 - 请尽量保持精准简洁。";
        }
        SystemMessage enhancedSystemMessage;
        if (request.getSystemMessage() == null) {
            enhancedSystemMessage = new SystemMessage(basePrompt);
        } else {
            enhancedSystemMessage = new SystemMessage(request.getSystemMessage().getText() + "\n\n" + basePrompt);
        }
        ModelRequest enhancedRequest = ModelRequest.builder(request).systemMessage(enhancedSystemMessage).build();
        return handler.call(enhancedRequest);
    }

    @Override
    public String getName() {
        return "StateAwarePromptInterceptor";
    }
}
