package org.huan.demo.ai.tools;

import org.huan.demo.ai.record.Request;
import org.huan.demo.ai.record.Response;

public class WebSearchTool {
    public Response search(Request request){
        // 实际实现中调用网络搜索 API
        return new Response("网络搜索结果："+request.query());
    }
}
