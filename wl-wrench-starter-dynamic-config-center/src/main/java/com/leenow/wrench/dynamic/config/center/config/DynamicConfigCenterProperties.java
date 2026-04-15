package com.leenow.wrench.dynamic.config.center.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 动态配置中心配置文件
 *
 */
@ConfigurationProperties(prefix = "wl.wrench.config", ignoreInvalidFields = true)
public class DynamicConfigCenterProperties {

    /**
     * 系统名称
     */
    private String system;

    public String getKey(String attributeName) {
        return this.system + "_" + attributeName;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

}
