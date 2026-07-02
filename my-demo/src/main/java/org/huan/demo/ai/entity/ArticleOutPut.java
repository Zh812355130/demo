package org.huan.demo.ai.entity;

import lombok.Data;

@Data
public class ArticleOutPut {
    private String title;
    private String content;
    private int characterCount;
}
