package org.huan.demo.ai.tools;

import org.huan.demo.ai.record.Request;
import org.huan.demo.ai.record.Response;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentSearchTool {
    private final VectorStore vectorStore;

    public DocumentSearchTool(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public Response search(Request request){
        List<Document> docs
                = vectorStore.similaritySearch(SearchRequest.builder().query(request.query()).topK(5).build());
        String content = docs.stream().map(Document::getText).collect(Collectors.joining("\n\n"));
        return new Response(content);
    }

}
