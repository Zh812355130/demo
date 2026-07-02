package org.huan.mcp.controller;

import lombok.RequiredArgsConstructor;
import org.huan.mcp.dto.ChatDto;
import org.huan.mcp.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatDto chatDto) {
        return chatService.chatStream(chatDto.getQuestion(), chatDto.getSessionId());
    }


}
