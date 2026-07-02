package org.huan.demo.ai.entity;

import lombok.Data;

import java.util.List;

@Data
public class ReviewOutPut {

    private String comment;

    private boolean approved;

    private List<String> suggestions;

}
