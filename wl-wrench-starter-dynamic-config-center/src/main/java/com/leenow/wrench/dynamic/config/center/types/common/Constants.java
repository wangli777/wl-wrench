package com.leenow.wrench.dynamic.config.center.types.common;

/**
 * @author: WangLi
 * @date: 2026/4/15 19:26
 * @description:
 */
public class Constants {

    public final static String DYNAMIC_CONFIG_CENTER_REDIS_TOPIC = "DYNAMIC_CONFIG_CENTER_REDIS_TOPIC_";

    public final static String SYMBOL_COLON = ":";

    public static String getTopic(String application) {
        return DYNAMIC_CONFIG_CENTER_REDIS_TOPIC + application;
    }

}
