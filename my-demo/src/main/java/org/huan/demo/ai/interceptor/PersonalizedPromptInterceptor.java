package org.huan.demo.ai.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import org.huan.demo.ai.entity.UserPreferences;
import org.huan.demo.ai.util.UserPreferenceStore;
import org.springframework.ai.chat.messages.SystemMessage;

public class PersonalizedPromptInterceptor extends ModelInterceptor {

    private UserPreferenceStore store;

    public PersonalizedPromptInterceptor(UserPreferenceStore store) {
        this.store = store;
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        String userId = getUserIdFromContext(request);
        UserPreferences prefs = store.getPreferences(userId);
        String personalizedPrompt = buildPersonalizedPrompt(prefs);
        SystemMessage enhancedSystemMessage;
        if(request.getSystemMessage() == null){
            enhancedSystemMessage = new SystemMessage(personalizedPrompt);
        }else{
            enhancedSystemMessage = new SystemMessage(request.getSystemMessage() + "\n\n" + personalizedPrompt);
        }
        ModelRequest enhancedRequest = ModelRequest.builder(request).systemMessage(enhancedSystemMessage).build();
        return handler.call(enhancedRequest);
    }

    private String getUserIdFromContext(ModelRequest request){
        return (String) request.getContext().get("user-id");
    }


    private String buildPersonalizedPrompt(UserPreferences prefs){
        StringBuilder prompt = new StringBuilder("你是一个有用的助手。");
        if(prefs.getCommunicationStyle() != null){
            prompt.append("\n\n沟通风格：").append(prefs.getCommunicationStyle());
        }
        if(prefs.getLanguage() != null){
            prompt.append("\n\n使用语言：").append(prefs.getLanguage());
        }
        if(!prefs.getInterests().isEmpty()){
            prompt.append("\n\n用户兴趣：").append(String.join("，",prefs.getInterests()));
        }
        return prompt.toString();
    }

    @Override
    public String getName() {
        return "PersonalizedPromptInterceptor";
    }
}
