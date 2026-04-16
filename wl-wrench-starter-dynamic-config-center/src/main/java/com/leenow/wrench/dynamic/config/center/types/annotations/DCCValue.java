package com.leenow.wrench.dynamic.config.center.types.annotations;

import java.lang.annotation.*;

/**
 * 动态配置中心值注解
 * 
 * <p>用于标记需要动态配置的字段，支持运行时通过 Redis 动态更新字段值。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * @Service
 * public class MyService {
 *     
 *     // 配置格式：配置名：默认值
 *     @DCCValue("downgradeSwitch:0")
 *     private String downgradeSwitch;
 *     
 *     // 支持多个字段使用同一个配置 Key
 *     @DCCValue("cache.expire.time:300")
 *     private Integer userCacheExpire;
 *     
 *     @DCCValue("cache.expire.time:300")
 *     private Integer productCacheExpire;
 * }
 * }
 * </pre>
 * 
 * <h3>配置格式说明：</h3>
 * <ul>
 *     <li>格式：{@code configKey:defaultValue}</li>
 *     <li>{@code configKey} - 配置项名称（必填）</li>
 *     <li>{@code defaultValue} - 默认值（必填，防止配置缺失）</li>
 * </ul>
 * 
 * <h3>支持的数据类型：</h3>
 * <ul>
 *     <li>基本类型：String, Integer, Long, Double, Float, Boolean, Short, Byte</li>
 *     <li>枚举类型：自动转换为对应的枚举值</li>
 * </ul>
 * 
 * @author WangLi
 * @date 2026/4/15 19:23
 * @see com.leenow.wrench.dynamic.config.center.domain.service.DynamicConfigCenterService
 */
@Retention(RetentionPolicy.RUNTIME)  // 注解在运行时保留，可通过反射读取
@Target({ElementType.FIELD})          // 注解只能用于字段上
@Documented                           // 生成 JavaDoc 时包含此注解
public @interface DCCValue {
    /**
     * 配置项，格式为 "configKey:defaultValue"
     * 
     * @return 配置项名称和默认值，例如 "downgradeSwitch:0"
     */
    String value() default "";
}
