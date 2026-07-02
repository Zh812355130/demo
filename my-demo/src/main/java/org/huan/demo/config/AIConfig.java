package org.huan.demo.config;


import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import jakarta.annotation.Resource;
import org.huan.demo.config.properties.AliProperties;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AliProperties.class)
public class AIConfig {
    @Resource
    private AliProperties aliProperties;

    @Value("${spring.ai.dashscope.chat.options.model}")
    private String model;


    //a2a server
    @Bean(name = "dataAnalysisAgent")
    public ReactAgent dataAnalysisAgent(@Qualifier("dashScopeChatModel") ChatModel chatModel) {
        return ReactAgent.builder()
                .name("data_analysis_agent")
                .model(chatModel)
                .description("专门用于数据分析和统计计算的本地智能体")
                .instruction("你是一个专业的数据分析专家，擅长处理各类数据统计和分析任务。你能够理解用户的数据分析需求，提供准确的统计计算结果和专业的分析建议")
                .outputKey("messages")
                .build();

    }

    @Bean
    public DashScopeChatModel dashScopeChatModel() {
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(aliProperties.getApiKey()).build();
        DashScopeChatOptions options = DashScopeChatOptions.builder().model(model).build();
        return DashScopeChatModel.builder().dashScopeApi(dashScopeApi).defaultOptions(options).build();
    }

}
