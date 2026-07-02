package org.huan.mcp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;

    @Override
    public Flux<String> chatStream(String question, String sessionId) {
        return chatClient.prompt().user(question).stream().content().concatWith(Flux.just("[END]"));
    }
}
