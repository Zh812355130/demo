package org.huan.mcp.config;

import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.client.common.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AIConfig {
    @Bean
    public ChatClient chatClient(ChatModel chatModel, ToolCallbackProvider provider) {
        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(provider.getToolCallbacks())
                .build();
    }

    @Bean
    public List<NamedClientMcpTransport> amapMcpClientTransport(){
        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder("https://mcp.amap.com")
                .sseEndpoint("/sse?key=you-key").build();
        return List.of(new NamedClientMcpTransport("amap", transport));
    }

}
