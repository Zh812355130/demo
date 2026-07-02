package org.huan.demo.ai.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatModel;

public class AnswerValidationInterceptor extends ModelInterceptor {

    private final ChatModel chatModel;
    private static final double MIN_CONFIDENCE = 0.7;

    public AnswerValidationInterceptor(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        ModelResponse response = handler.call(request);
        //验证答案
        AssistantMessage message = (AssistantMessage) response.getMessage();
        boolean isValid = validateAnswer(message.getText(),request);
        if(!isValid){
            SystemMessage validationPrompt = new SystemMessage("请重新检查你的答案，确保基于提供的上下文信息，并且准确完整");
            ModelRequest retryRequest = ModelRequest.builder(request)
                    .systemMessage(validationPrompt)
                    .build();
            return handler.call(retryRequest);
        }
        return response;
    }


    private boolean validateAnswer(String answer,ModelRequest request){
        //实际可以使用LLM来验证答案
        return answer!=null && answer.length() > 20;
    }

    @Override
    public String getName() {
        return "answer_validation";
    }


}
