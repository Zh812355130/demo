package org.huan.demo.ai.util;

import org.huan.demo.ai.entity.UserPreferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPreferenceStore {
    private Map<String, UserPreferences> store = new HashMap<>();

    public UserPreferences getPreferences(String userId) {
        return store.getOrDefault(userId, new UserPreferences("专业", "中文", List.of()));
    }

    public void savePreferences(String userId, UserPreferences preferences) {
        store.put(userId, preferences);
    }

}
