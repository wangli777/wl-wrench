package com.leenow.wrench.design.framework.strategy.annotation;

import java.lang.annotation.*;

/**
 * 事实注解
 * 
 * <p>用于标记方法参数，表示该参数是一个事实（Fact），
 * 会从 DynamicContext 中自动注入对应的值。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * @Strategy(name = "weatherStrategy")
 * public class WeatherStrategy {
 *     
 *     @Condition
 *     public boolean match(@Fact("weather") String weather) {
 *         // weather 参数会自动从 DynamicContext 中注入
 *         return "rainy".equals(weather);
 *     }
 *     
 *     @Action
 *     public Response apply(@Fact("user") User user) {
 *         // user 参数会自动从 DynamicContext 中注入
 *         return new Response(user.getDiscount());
 *     }
 * }
 * }
 * </pre>
 * 
 * @author WangLi
 * @date 2026/4/16
 * @see com.leenow.wrench.design.framework.strategy.DynamicContext
 */
@Target(ElementType.PARAMETER)  // 注解用于方法参数上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留
@Documented
public @interface Fact {
    
    /**
     * 事实名称
     * 
     * <p>对应 DynamicContext 中的 key，用于从上下文中获取值。</p>
     * 
     * @return 事实名称
     */
    String value();
}
