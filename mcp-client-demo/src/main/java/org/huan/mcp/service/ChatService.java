package org.huan.mcp.service;

import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<String> chatStream(String question,String sessionId);
}
