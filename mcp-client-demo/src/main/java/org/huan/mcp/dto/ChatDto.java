package org.huan.mcp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatDto {

    private String question;
    private String sessionId;

}
