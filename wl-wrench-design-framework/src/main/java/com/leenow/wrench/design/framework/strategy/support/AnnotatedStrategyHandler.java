package com.leenow.wrench.design.framework.strategy.support;

import com.leenow.wrench.design.framework.strategy.DynamicContext;
import com.leenow.wrench.design.framework.strategy.StrategyHandler;
import com.leenow.wrench.design.framework.strategy.annotation.Action;
import com.leenow.wrench.design.framework.strategy.annotation.Condition;
import com.leenow.wrench.design.framework.strategy.base.BaseRequest;
import com.leenow.wrench.design.framework.strategy.base.BaseResponse;

/**
 * 注解策略处理器
 * 
 * <p>支持使用注解定义策略的处理器，自动解析 {@link Condition} 和 {@link Action} 注解。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * @Strategy(name = "discountStrategy", description = "折扣策略")
 * public class DiscountStrategy extends AnnotatedStrategyHandler<Request, Response> {
 *     
 *     @Condition
 *     public boolean match(Request request) {
 *         return request.getAmount() >= 100;
 *     }
 *     
 *     @Action
 *     public Response apply(Request request) {
 *         return new Response(0.9);
 *     }
 * }
 * }
 * </pre>
 * 
 * @param <T> 请求参数类型
 * @param <R> 返回值类型
 * 
 * @author WangLi
 * @date 2026/4/16
 */
public abstract class AnnotatedStrategyHandler<T extends BaseRequest,D extends DynamicContext, R extends BaseResponse> implements StrategyHandler<T, D, R> {
    
    /**
     * 条件匹配方法
     * 
     * <p>自动扫描并执行所有标注了 {@link Condition} 注解的方法。</p>
     * 
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @return 如果所有条件都匹配返回 true，否则返回 false
     * @throws Exception 如果条件执行失败
     */
    @Override
    public boolean match(T requestParameter, D dynamicContext) throws Exception {
        return AnnotationSupport.matchCondition(this, requestParameter, dynamicContext);
    }
    
    /**
     * 应用策略方法
     * 
     * <p>自动扫描并执行标注了 {@link Action} 注解的方法。</p>
     * 
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @return 策略执行结果
     * @throws Exception 如果动作执行失败
     */
    @Override
    public  R apply(T requestParameter, D dynamicContext) throws Exception {
        return AnnotationSupport.executeAction(this, requestParameter, dynamicContext);
    }
}
