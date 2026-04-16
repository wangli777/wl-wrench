package com.leenow.wrench.dynamic.config.center.domain.model.vo;

/**
 * 属性值调整值对象（Value Object）
 * 
 * <p>用于封装动态配置更新的属性信息，在 Redis 发布/订阅模式中传输。</p>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>Redis 发布配置更新消息时，使用此对象作为消息载体</li>
 *     <li>监听器接收消息后，解析此对象并更新对应的字段值</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * // 创建属性变更对象
 * AttributeVO attributeVO = new AttributeVO("downgradeSwitch", "8");
 * 
 * // 发布到 Redis Topic
 * rTopic.publish(attributeVO);
 * 
 * // 监听器接收后会解析并更新对应字段
 * }
 * </pre>
 * 
 * @Description: 属性值调整值对象
 * @Author: WangLi
 * @Date: 2026/4/15
 */
public class AttributeVO {

    /** 
     * 属性名称（配置 Key）
     * 
     * <p>例如：{@code "downgradeSwitch"}, {@code "cache.expire.time"}</p>
     */
    private String attribute;

    /** 
     * 新的属性值
     * 
     * <p>所有类型的值都以 String 形式存储，在注入时进行类型转换</p>
     */
    private String value;

    /**
     * 默认构造函数
     */
    public AttributeVO() {
    }

    /**
     * 带参构造函数
     * 
     * @param attribute 属性名称（配置 Key）
     * @param value 新的属性值
     */
    public AttributeVO(String attribute, String value) {
        this.attribute = attribute;
        this.value = value;
    }

    /**
     * 获取属性名称
     * 
     * @return 属性名称
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * 设置属性名称
     * 
     * @param attribute 属性名称
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    /**
     * 获取属性值
     * 
     * @return 属性值
     */
    public String getValue() {
        return value;
    }

    /**
     * 设置属性值
     * 
     * @param value 属性值
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 重写 toString 方法，便于日志输出
     * 
     * <p>输出 JSON 格式，方便调试和日志分析。</p>
     * 
     * @return JSON 格式的字符串表示
     */
    @Override
    public String toString() {
        return "{"
                + "\"attribute\":\""
                + attribute + '\"'
                + ",\"value\":\""
                + value + '\"'
                + "}";
    }
}
