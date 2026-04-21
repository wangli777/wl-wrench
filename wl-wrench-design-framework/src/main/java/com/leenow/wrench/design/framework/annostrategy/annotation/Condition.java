package com.leenow.wrench.design.framework.annostrategy.annotation;

import java.lang.annotation.*;

/**
 * 条件注解
 * 
 * <p>用于标记策略的匹配条件方法。被注解的方法必须返回 boolean 类型，
 * 返回 true 表示条件匹配，策略会被激活执行。</p>
 * 
 * <h3>使用要求：</h3>
 * <ul>
 *     <li>方法返回值必须是 boolean 类型</li>
 *     <li>可以接受请求参数和上下文参数</li>
 *     <li>不应该有副作用（side-effect）</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * @Strategy(name = "weatherStrategy")
 * public class WeatherStrategy {
 *     
 *     @Condition
 *     public boolean match(@Fact("weather") String weather) {
 *         return "rainy".equals(weather);
 *     }
 *     
 *     @Action
 *     public Response apply(Request request) {
 *         // 业务逻辑
 *     }
 * }
 * }
 * </pre>
 * 
 * @author WangLi
 * @date 2026/4/16
 * @see Action
 */
@Target(ElementType.METHOD)  // 注解用于方法上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留
@Documented
public @interface Condition {
    
    /**
     * 条件描述
     * 
     * <p>可选的描述信息，用于说明条件的用途。</p>
     * 
     * @return 条件描述，默认为空
     */
    String description() default "";
}
