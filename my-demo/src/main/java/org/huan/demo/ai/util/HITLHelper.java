package org.huan.demo.ai.util;

import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;

public class HITLHelper {

    public static InterruptionMetadata approveAll(InterruptionMetadata interruptionMetadata) {
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());
        interruptionMetadata.toolFeedbacks().forEach(toolFeedback -> {
            builder.addToolFeedback(InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                    .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                    .build());
        });
        return builder.build();
    }

    public static InterruptionMetadata rejectAll(InterruptionMetadata interruptionMetadata, String reason) {
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());
        interruptionMetadata.toolFeedbacks().forEach(toolFeedback -> {
            builder.addToolFeedback(InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                    .result(InterruptionMetadata.ToolFeedback.FeedbackResult.REJECTED)
                    .description(reason)
                    .build());
        });
        return builder.build();
    }


    public static InterruptionMetadata editTool(InterruptionMetadata interruptionMetadata, String toolName, String newArguments) {
        InterruptionMetadata.Builder builder = InterruptionMetadata.builder()
                .nodeId(interruptionMetadata.node())
                .state(interruptionMetadata.state());
        interruptionMetadata.toolFeedbacks().forEach(toolFeedback -> {
            if (toolFeedback.getName().equals(toolName)) {
                builder.addToolFeedback(InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                        .arguments(newArguments)
                        .result(InterruptionMetadata.ToolFeedback.FeedbackResult.EDITED)
                        .build());
            } else {
                builder.addToolFeedback(InterruptionMetadata.ToolFeedback.builder(toolFeedback)
                        .result(InterruptionMetadata.ToolFeedback.FeedbackResult.APPROVED)
                        .build());
            }
        });
        return builder.build();
    }

}
