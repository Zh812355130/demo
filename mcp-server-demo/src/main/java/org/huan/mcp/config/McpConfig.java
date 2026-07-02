package org.huan.mcp.config;

import org.huan.mcp.tools.WeatherService;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpConfig {
    @Bean
    public List<ToolCallback> weatherTools(WeatherService weatherService){
        return List.of(ToolCallbacks.from(weatherService));
    }

}
