package org.huan.demo.ai.hooks;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.ModelHook;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LoggingModelHook extends ModelHook {
    @Override
    public String getName() {
        return "logging_model_hook";
    }

    @Override
    public HookPosition[] getHookPositions() {
        return new HookPosition[]{HookPosition.BEFORE_MODEL, HookPosition.AFTER_MODEL};
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeModel(OverAllState state, RunnableConfig config) {
        List<?> messages = (List<?>) state.value("messages").orElse(List.of());
        System.out.println("调用模型前 - 消息数："+messages.size());
        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterModel(OverAllState state, RunnableConfig config) {
        System.out.println("模型调用后 - 响应已生成");
        return CompletableFuture.completedFuture(Map.of());
    }
}
