package com.leenow.wrench.design.framework.annostrategy;


import com.leenow.wrench.design.framework.strategy.DynamicContext;
import com.leenow.wrench.design.framework.strategy.base.BaseRequest;
import com.leenow.wrench.design.framework.strategy.base.BaseResponse;

/**
 * 策略处理器接口
 * 
 * <p>定义策略处理器的基本行为，包括条件匹配和策略执行。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * public class DiscountStrategy implements StrategyHandler<Request, DynamicContext, Response> {
 *     
 *     @Override
 *     public boolean match(Request request, DynamicContext context) {
 *         // 条件匹配逻辑
 *         return request.getAmount() >= 100;
 *     }
 *     
 *     @Override
 *     public Response apply(Request request, DynamicContext context) {
 *         // 策略执行逻辑
 *         return new Response(0.9);
 *     }
 * }
 * }
 * </pre>
 * 
 * @param <T> 请求参数类型
 * @param <D> 上下文类型（必须是 DynamicContext 或其子类）
 * @param <R> 返回值类型
 * 
 * @author WangLi
 * @date 2026/4/16
 */
public interface StrategyHandler<T extends BaseRequest, D extends DynamicContext, R extends BaseResponse> {

    /**
     * 条件匹配方法
     * 
     * <p>判断当前策略是否适用于给定的请求和上下文。</p>
     * <p>默认实现返回 true，表示总是匹配。</p>
     * 
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @return 如果匹配返回 true，否则返回 false
     * @throws Exception 如果匹配过程发生错误
     */
    default boolean match(T requestParameter, D dynamicContext) throws Exception {
        return true;
    }

    /**
     * 执行策略
     * 
     * <p>执行具体的业务逻辑。</p>
     * 
     * @param requestParameter 请求参数
     * @param dynamicContext 动态上下文
     * @return 执行结果
     * @throws Exception 如果执行过程发生错误
     */
    R handle(T requestParameter, D dynamicContext) throws Exception;


}
