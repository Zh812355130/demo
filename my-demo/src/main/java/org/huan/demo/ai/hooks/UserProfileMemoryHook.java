package org.huan.demo.ai.hooks;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;
import com.alibaba.cloud.ai.graph.store.Store;
import com.alibaba.cloud.ai.graph.store.StoreItem;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserProfileMemoryHook extends ModelHook {
    @Override
    public String getName() {
        return "MemoryHook";
    }

    @Override
    public HookPosition[] getHookPositions() {
        return new HookPosition[]{HookPosition.BEFORE_MODEL, HookPosition.AFTER_MODEL};
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        String userId = (String) config.metadata("user_id").orElse(null);
        if (userId == null) {
            return CompletableFuture.completedFuture(Map.of());
        }
        Store store = config.store();
        Optional<StoreItem> itemOpt = store.getItem(List.of("user_profile"), userId);
        if (itemOpt.isPresent()) {
            Map<String, Object> profile = itemOpt.get().getValue();
            String userContext = String.format("用户信息：姓名=%s,年龄=%s，邮箱=%s，偏好=%s",
                    profile.get("name"),
                    profile.get("age"),
                    profile.get("email"),
                    profile.get("preferences")
            );
            List<Message> messages = state.value("messages", List.of());
            List<Message> newMessages = new ArrayList<>();
            SystemMessage existingSystemMessage = null;
            int systemMessageIndex = -1;
            for (int i = 0; i < messages.size(); i++) {
                Message msg = messages.get(i);
                if(msg instanceof SystemMessage){
                    existingSystemMessage = (SystemMessage) msg;
                    systemMessageIndex = i;
                    break;
                }
            }
            SystemMessage enhancedSystemMessage;
            if(existingSystemMessage != null){
                enhancedSystemMessage = new SystemMessage(existingSystemMessage.getText() +"\n\n"+userContext);
            }else{
                enhancedSystemMessage = new SystemMessage(userContext);
            }
            if(systemMessageIndex >= 0 ){
                for (int i = 0; i < messages.size(); i++) {
                    if(i == systemMessageIndex){
                        newMessages.add(enhancedSystemMessage);
                    }else{
                        newMessages.add(messages.get(i));
                    }
                }
            }else{
                newMessages.add(enhancedSystemMessage);
                newMessages.addAll(messages);
            }
            return CompletableFuture.completedFuture(Map.of("messages", newMessages));
        }
        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        return CompletableFuture.completedFuture(Map.of());
    }
}
