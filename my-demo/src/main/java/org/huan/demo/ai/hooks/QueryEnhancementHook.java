package org.huan.demo.ai.hooks;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@HookPositions({HookPosition.BEFORE_AGENT})
public class QueryEnhancementHook extends AgentHook {

    private final ChatModel chatModel;
    private static final String ENHANCED_QUERY_KEY = "enhanced_query";
    public QueryEnhancementHook(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String getName() {
        return "query_enhancement";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        Optional<Object> messagesOpt = state.value("messages");
        if(messagesOpt.isEmpty()){
            return CompletableFuture.completedFuture(Map.of());
        }
        List<Message> messages = (List<Message>) messagesOpt.get();
        String userQuery = messages.stream()
                .filter(msg -> msg instanceof UserMessage)
                .map(msg -> ((UserMessage) msg).getText())
                .reduce((first, second) -> second)
                .orElse("");
        if(userQuery.isEmpty()){
            return CompletableFuture.completedFuture(Map.of());
        }

        String enhancedQuery = enhanceQuery(userQuery);
        if(!enhancedQuery.equals(userQuery)){
            List<Message> enhancedMessages = new ArrayList<>();
            for (Message msg : messages) {
                if(msg instanceof UserMessage){
                    enhancedMessages.add(new UserMessage(enhancedQuery));
                }else{
                    enhancedMessages.add(msg);
                }
            }
            config.metadata().ifPresent(meta-> meta.put(ENHANCED_QUERY_KEY, enhancedQuery));
            return CompletableFuture.completedFuture(Map.of("messages",enhancedMessages));
        }
        return CompletableFuture.completedFuture(Map.of());
    }

    private String enhanceQuery(String query){
        RewriteQueryTransformer build = RewriteQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(chatModel))
                .build();
        return build.transform(Query.builder().text(query).build()).text();
    }

}
