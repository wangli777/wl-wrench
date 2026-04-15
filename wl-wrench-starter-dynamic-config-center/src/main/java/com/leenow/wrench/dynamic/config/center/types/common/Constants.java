package com.leenow.wrench.dynamic.config.center.types.common;

public class Constants {

    public static final String DYNAMIC_CONFIG_CENTER_REDIS_TOPIC = "DYNAMIC_CONFIG_CENTER_REDIS_TOPIC_";

    public static final String SYMBOL_COLON = ":";

    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    public static String getTopic(String application) {
        return DYNAMIC_CONFIG_CENTER_REDIS_TOPIC + application;
    }

}
