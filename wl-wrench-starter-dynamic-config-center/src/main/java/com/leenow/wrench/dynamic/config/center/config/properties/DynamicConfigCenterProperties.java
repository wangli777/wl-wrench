package com.leenow.wrench.dynamic.config.center.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 动态配置中心配置属性类
 * 
 * <p>用于读取和存储 application.yml 中的动态配置中心相关配置。</p>
 * 
 * <h3>配置示例：</h3>
 * <pre>
 * {@code
 * wl:
 *   wrench:
 *     config:
 *       system: test-system  # 系统名称，用于构建配置 Key
 * }
 * </pre>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *     <li>读取系统名称配置</li>
 *     <li>构建完整的配置 Key：{@code systemName_attributeName}</li>
 *     <li>支持配置热刷新（通过 {@code @ConfigurationProperties}）</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * DynamicConfigCenterProperties properties = ...;  // Spring 注入
 * String configKey = properties.getKey("downgradeSwitch");
 * // 结果：test-system_downgradeSwitch
 * }
 * </pre>
 * 
 * @author WangLi
 * @see org.springframework.boot.context.properties.ConfigurationProperties
 */
@ConfigurationProperties(prefix = "wl.wrench.config", ignoreInvalidFields = true)
public class DynamicConfigCenterProperties {

    /**
     * 系统名称
     * 
     * <p>用于构建配置 Key 的前缀，不同系统使用不同的前缀避免配置冲突。</p>
     * <p>例如：{@code test-system}, {@code order-system}, {@code user-system}</p>
     */
    private String system;

    /**
     * 构建完整的配置 Key
     * 
     * <p>将属性名与系统名称组合，形成唯一的配置 Key。</p>
     * 
     * @param attributeName 属性名称（不包含系统前缀）
     * @return 完整的配置 Key，格式为 {@code systemName_attributeName}
     * 
     * <h3>示例：</h3>
     * <pre>
     * {@code
     * properties.setSystem("test-system");
     * String key = properties.getKey("downgradeSwitch");
     * // 结果：test-system_downgradeSwitch
     * }
     * </pre>
     */
    public String getKey(String attributeName) {
        return this.system + "_" + attributeName;
    }

    /**
     * 获取系统名称
     * 
     * @return 系统名称
     */
    public String getSystem() {
        return system;
    }

    /**
     * 设置系统名称
     * 
     * @param system 系统名称
     */
    public void setSystem(String system) {
        this.system = system;
    }

}
