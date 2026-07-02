package org.huan.demo.ai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserPreferences {

    private String communicationStyle;
    private String language;
    private List<String> interests;


}
