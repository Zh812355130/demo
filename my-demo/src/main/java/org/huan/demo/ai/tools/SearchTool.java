package org.huan.demo.ai.tools;

import org.springframework.ai.chat.model.ToolContext;

import java.util.function.BiFunction;

public class SearchTool implements BiFunction<String, ToolContext, String> {

    @Override
    public String apply(String query, ToolContext toolContext) {
        return "搜索结果：" + query;
    }
}
