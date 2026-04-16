package com.leenow.wrench.dynamic.config.center.types.common;

/**
 * 动态配置中心常量类
 * 
 * <p>定义系统中使用的所有常量，包括 Redis Topic 前缀、符号常量等。</p>
 * 
 * <h3>设计原则：</h3>
 * <ul>
 *     <li>使用私有构造函数防止实例化</li>
 *     <li>所有常量使用 {@code public static final} 修饰</li>
 *     <li>提供工具方法简化常量使用</li>
 * </ul>
 * 
 * @author WangLi
 * @date 2026/4/15
 */
public class Constants {

    /**
     * 动态配置中心 Redis Topic 前缀
     * 
     * <p>用于构建 Redis 发布/订阅模式的 Topic 名称。</p>
     * <p>完整 Topic 名称格式：{@code DYNAMIC_CONFIG_CENTER_REDIS_TOPIC_ + systemName}</p>
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * {@code
     * String topicName = Constants.getTopic("test-system");
     * // 结果：DYNAMIC_CONFIG_CENTER_REDIS_TOPIC_test-system
     * }
     * </pre>
     */
    public static final String DYNAMIC_CONFIG_CENTER_REDIS_TOPIC = "DYNAMIC_CONFIG_CENTER_REDIS_TOPIC_";

    /**
     * 冒号分隔符常量
     * 
     * <p>用于解析配置格式，例如：{@code "configKey:defaultValue"}</p>
     */
    public static final String SYMBOL_COLON = ":";

    /**
     * 私有构造函数，防止类被实例化
     * 
     * <p>此类是工具类，只包含静态常量和静态方法，不应该被实例化。</p>
     * 
     * @throws UnsupportedOperationException 如果尝试实例化则抛出此异常
     */
    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    /**
     * 构建 Redis Topic 名称
     * 
     * <p>根据应用名称生成完整的 Redis Topic 名称。</p>
     * 
     * @param application 应用/系统名称，例如 "test-system"
     * @return 完整的 Topic 名称，例如 "DYNAMIC_CONFIG_CENTER_REDIS_TOPIC_test-system"
     */
    public static String getTopic(String application) {
        return DYNAMIC_CONFIG_CENTER_REDIS_TOPIC + application;
    }

}
