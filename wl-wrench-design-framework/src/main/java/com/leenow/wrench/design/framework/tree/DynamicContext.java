package com.leenow.wrench.design.framework.tree;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: WangLi
 * @date: 2026/4/16 12:55
 * @description:
 */
public class DynamicContext {

    private Map<String, Object> dataObjects = new HashMap<>();

    public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
    }

    public <T> T getValue(String key) {
        return (T) dataObjects.get(key);
    }
}
