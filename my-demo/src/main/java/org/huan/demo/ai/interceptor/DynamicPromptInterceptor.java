package org.huan.demo.ai.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import org.springframework.ai.chat.messages.SystemMessage;

public class DynamicPromptInterceptor extends ModelInterceptor {

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        String userRole = (String) request.getContext().getOrDefault("user_role","default");
        String dynamicPrompt = switch (userRole){
            case "expert" -> """
                    你正在与技术专家对话。
                        - 使用主页术语
                        - 深入技术细节
                    """;
            case "beginner"-> """
                    你正在与初学者对话。
                        - 使用简单语言
                        - 解释基础概念
                    """;
            default -> "你是一个专业的助手，保持友好和专业";
        };
        SystemMessage enhancedSystemMessage;
        if(request.getSystemMessage() == null){
            enhancedSystemMessage = new SystemMessage(dynamicPrompt);
        }else{
            enhancedSystemMessage = new SystemMessage(request.getSystemMessage().getText()+"\n\n"+dynamicPrompt);
        }
        ModelRequest modified = ModelRequest.builder(request).systemMessage(enhancedSystemMessage).build();
        return handler.call(modified);
    }

    @Override
    public String getName() {
        return "DynamicPromptInterceptor";
    }
}
