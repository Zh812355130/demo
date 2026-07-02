package org.huan.demo.ai.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;

public class ToolErrorInterceptor extends ToolInterceptor {

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        try {
            System.out.println("========== 原始Tool Call ==========");
            System.out.println("工具名: " + request.getToolName());
            System.out.println("参数JSON: " + request.getArguments());
            return handler.call(request);
        }catch (Exception e){
            return ToolCallResponse.of(request.getToolCallId(),request.getToolName(),"Tool failed: "+e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "ToolErrorInterceptor";
    }
}
