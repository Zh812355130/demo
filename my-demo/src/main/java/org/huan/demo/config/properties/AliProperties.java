package org.huan.demo.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.ai.dashscope")
public class AliProperties {
    private String apiKey;
}
