package com.leenow.wrench.design.framework.strategy.annotation;

import java.lang.annotation.*;

/**
 * 策略注解
 * 
 * <p>用于标记一个类为策略处理器，包含策略的元数据信息。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * @Strategy(name = "discountStrategy", description = "折扣策略", priority = 1)
 * public class DiscountStrategy implements StrategyHandler<Request, Response> {
 *     
 *     @Condition
 *     public boolean match(Request request, DynamicContext context) {
 *         return request.getAmount() >= 100;
 *     }
 *     
 *     @Action
 *     @Override
 *     public Response apply(Request request, DynamicContext context) {
 *         // 业务逻辑
 *     }
 * }
 * }
 * </pre>
 * 
 * @author WangLi
 * @date 2026/4/16
 * @see com.leenow.wrench.design.framework.strategy.StrategyHandler
 */
@Target(ElementType.TYPE)  // 注解用于类上
@Retention(RetentionPolicy.RUNTIME)  // 运行时保留，可通过反射读取
@Documented  // 生成 JavaDoc 时包含
public @interface Strategy {
    
    /**
     * 策略名称
     * 
     * <p>用于唯一标识一个策略，可以通过名称获取对应的策略处理器。</p>
     * 
     * @return 策略名称
     */
    String name();
    
    /**
     * 策略描述
     * 
     * <p>描述策略的用途和功能，便于文档化和理解。</p>
     * 
     * @return 策略描述，默认为空
     */
    String description() default "";
    
    /**
     * 策略优先级
     * 
     * <p>数值越小优先级越高，在责任链模式中用于确定执行顺序。</p>
     * 
     * @return 优先级数值，默认为 0
     */
    int priority() default 0;
}
