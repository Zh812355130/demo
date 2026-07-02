package org.huan.demo.ai.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageFilterInterceptor extends ModelInterceptor {
    private final int maxMessages;

    public MessageFilterInterceptor(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        List<Message> messages = request.getMessages();
        if (messages.size() > maxMessages) {
            List<Message> filtered = new ArrayList<>();
            messages.stream().filter(m -> m instanceof SystemMessage)
                    .findFirst().ifPresent(filtered::add);
            int startIndex = Math.max(0, messages.size() - maxMessages + 1);
            filtered.addAll(messages.subList(startIndex, messages.size()));
            messages = filtered;
        }
        ModelRequest enhancedRequest = ModelRequest.builder(request).messages(messages).build();
        return handler.call(enhancedRequest);
    }

    @Override
    public String getName() {
        return "MessageFilterInterceptor";
    }
}
