package com.leenow.wrench.design.framework.annostrategy.annotation;

import java.lang.annotation.*;

/**
 * 动作注解
 * 
 * <p>用于标记策略的执行方法。被注解的方法是策略的核心业务逻辑，
 * 当条件匹配时会被执行。</p>
 * 
 * <h3>使用要求：</h3>
 * <ul>
 *     <li>每个策略类必须有且只有一个@Action 注解的方法</li>
 *     <li>方法返回值类型应该与 StrategyHandler 的泛型 R 一致</li>
 *     <li>可以抛出异常，由框架统一处理</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * @Strategy(name = "discountStrategy")
 * public class DiscountStrategy {
 *     
 *     @Condition
 *     public boolean match(Request request) {
 *         return request.getAmount() >= 100;
 *     }
 *     
 *     @Action
 *     public Response apply(Request request, DynamicContext context) {
 *         // 计算折扣
 *         return new Response(0.9);
 *     }
 * }
 * }
 * </pre>
 * 
 * @author WangLi
 * @date 2026/4/16
 * @see Condition
 */
@Target(ElementType.METHOD)  // 注解用于方法上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留
@Documented
public @interface Action {
    
    /**
     * 动作描述
     * 
     * <p>可选的描述信息，用于说明动作的用途。</p>
     * 
     * @return 动作描述，默认为空
     */
    String description() default "";
}
