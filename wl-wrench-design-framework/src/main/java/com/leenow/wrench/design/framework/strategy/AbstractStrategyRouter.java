package com.leenow.wrench.design.framework.strategy;

import com.leenow.wrench.design.framework.strategy.base.BaseRequest;
import com.leenow.wrench.design.framework.strategy.base.BaseResponse;
import com.leenow.wrench.design.framework.strategy.exception.BusinessException;
import com.leenow.wrench.design.framework.strategy.exception.StrategyExecutionException;
import com.leenow.wrench.design.framework.strategy.exception.StrategyMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * 抽象策略路由器
 * 
 * <p>定义策略路由的完整生命周期，包括前置处理、上下文加载、业务处理、后置处理和异常处理。</p>
 * 
 * <h3>执行流程：</h3>
 * <ol>
 *     <li>applyBefore() - 前置处理，可以进行参数校验、权限检查等</li>
 *     <li>loadContext() - 加载上下文数据，准备业务处理所需的数据</li>
 *     <li>doApply() - 核心业务处理逻辑</li>
 *     <li>applyAfter() - 后置处理，可以进行日志记录、数据清理等</li>
 *     <li>applyAfterException() - 异常处理，记录异常信息</li>
 * </ol>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * public class DiscountStrategyRouter extends AbstractStrategyRouter<Request, DynamicContext, Response> {
 *     
 *     @Override
 *     protected Response doApply(Request request, DynamicContext context) {
 *         // 业务逻辑
 *         return new Response();
 *     }
 *     
 *     @Override
 *     protected void loadContext(Request request, DynamicContext context) {
 *         // 加载上下文数据
 *         context.setValue("user", request.getUser(), User.class);
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
public abstract class AbstractStrategyRouter<T extends BaseRequest, D extends DynamicContext, R extends BaseResponse>
        implements StrategyMapper<T, D, R>, StrategyHandler<T, D, R>{

    protected StrategyHandler<T, D, R> defaultStrategyHandler = StrategyHandler.DEFAULT_HANDLER;
    /**
     * 日志记录器
     * <p>用于记录策略执行过程中的各种事件</p>
     */
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @SuppressWarnings("unchecked")
    public D getContext() {
        return (D) DynamicContextHolder.getContext();
    }

    private static <D extends DynamicContext> void setContext(D dynamicContext) {
        DynamicContextHolder.setContext(dynamicContext);
    }

    /**
     * 应用策略方法 - 完整的生命周期
     * 
     * <p>按照预定义的生命周期执行策略：前置处理 -> 加载上下文 -> 业务处理 -> 后置处理 -> 异常处理</p>
     * 
     * @param requestParameter 请求参数
     * @return 处理结果
     * @throws Exception 如果执行过程中发生异常
     */
    @Override
    public R apply(T requestParameter) throws Exception {
        // 生成请求 ID，用于链路追踪
        String requestId = MDC.get("requestId");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString().replace("-", "");
            MDC.put("requestId", requestId);
        }
        
        long startTime = System.currentTimeMillis();
        String strategyName = this.getClass().getSimpleName();
        
        log.info("策略执行开始 - requestId: {}, strategy: {}, request: {}", 
                requestId, strategyName, requestParameter);

        try {

            // 1. 前置处理
            R applyBefore = applyBefore(requestParameter);
            if (applyBefore != null) {
                log.info("前置处理直接返回 - requestId: {}, strategy: {}", requestId, strategyName);
                return applyBefore;
            }

            // 2. 加载上下文
            D dynamicContext = loadContext(requestParameter);
            // 放到 ThreadLocal 中
            setContext(dynamicContext);
            log.debug("上下文加载完成 - requestId: {}, dynamicContext: {}",
                     requestId, dynamicContext);

            // 3. 核心业务处理
            R result = doApply(requestParameter);
            
            // 4. 后置处理
            applyAfter(requestParameter, result);
            
            // 记录执行时间
            long costTime = System.currentTimeMillis() - startTime;
            log.info("策略执行成功 - requestId: {}, strategy: {}, cost: {}ms", 
                    requestId, strategyName, costTime);
            
            return result;
            
        } catch (BusinessException e) {
            // 业务异常：只记录消息，不记录堆栈
            long costTime = System.currentTimeMillis() - startTime;
            log.warn("业务异常 - requestId: {}, strategy: {}, cost: {}ms, message: {}", 
                    requestId, strategyName, costTime, e.getMessage());
            
            applyAfterException(requestParameter, e);
            throw e;
            
        } catch (StrategyMappingException | StrategyExecutionException e) {
            // 策略相关异常：记录完整信息
            long costTime = System.currentTimeMillis() - startTime;
            log.error("策略异常 - requestId: {}, strategy: {}, cost: {}ms, message: {}", 
                     requestId, strategyName, costTime, e.getMessage(), e);
            
            applyAfterException(requestParameter, e);
            throw e;
            
        } catch (Exception e) {
            // 系统异常：包装后抛出
            long costTime = System.currentTimeMillis() - startTime;
            log.error("系统异常 - requestId: {}, strategy: {}, cost: {}ms", 
                     requestId, strategyName, costTime, e);
            
            applyAfterException(requestParameter, e);
            throw new StrategyExecutionException(
                String.format("策略执行失败 - requestId: %s, strategy: %s", requestId, strategyName), 
                e);
        } finally {
            // 手动清理上下文（兼容 JDK 1.8）
            try {

                DynamicContextHolder.clear();
                log.debug("上下文已清理 - requestId: {}", requestId);
            } catch (Exception e) {
                log.warn("清理上下文失败 - requestId: {}", requestId, e);
            }
            // 清理 MDC
            MDC.remove("requestId");
        }
    }

    /**
     * 路由方法 - 直接获取处理器并执行
     *
     * <p>不执行完整的生命周期，直接根据请求获取对应的处理器并执行。</p>
     *
     * @param requestParameter 请求参数
     * @return 处理结果
     * @throws Exception 如果执行过程中发生异常
     */
    public R router(T requestParameter) throws Exception {
        String requestId = MDC.get("requestId");
        if (requestId == null) {
            requestId = String.valueOf(System.currentTimeMillis());
        }
        
        log.debug("策略路由开始 - requestId: {}, strategy: {}", requestId, this.getClass().getSimpleName());
        
        // 获取下一个待执行的策略处理器
        StrategyHandler<T, D, R> handler = this.getNextHandler(requestParameter);
        
        // 如果没有找到处理器，抛出异常
        if (handler == null) {
            String message = String.format("未找到对应的策略处理器 - requestId: %s, strategy: %s", 
                    requestId, this.getClass().getSimpleName());
            log.error(message);
            throw new StrategyMappingException(message);
        }
        
        log.debug("策略处理器已找到 - requestId: {}, handler: {}", 
                 requestId, handler.getClass().getSimpleName());
        
        // 执行处理器
        return handler.apply(requestParameter);
    }

    /**
     * 前置处理
     * 
     * <p>在业务处理之前执行，可以进行参数校验、权限检查、限流等操作。</p>
     * <p>如果返回非 null 值，则直接返回，不再执行后续步骤。</p>
     * 
     * @param requestParameter 请求参数
     * @return 如果返回非 null 则直接返回，否则继续执行后续步骤
     * @throws Exception 如果前置处理失败
     */
    protected R applyBefore(T requestParameter) throws Exception {
        // 默认不做任何处理
        return null;
    }

    /**
     * 加载上下文
     * 
     * <p>从请求参数或其他来源加载业务处理所需的数据到上下文中。</p>
     * 
     * @param requestParameter 请求参数
     * @throws Exception 如果加载失败
     */
    protected abstract D loadContext(T requestParameter) throws Exception;

    /**
     * 核心业务处理
     *
     * <p>实现具体的业务逻辑，这是策略的核心部分。</p>
     *
     * @param requestParameter 请求参数
     * @return 业务处理结果
     * @throws Exception 如果业务处理失败
     */
    protected abstract R doApply(T requestParameter) throws Exception;

    /**
     * 后置处理
     * 
     * <p>在业务处理成功后执行，可以进行日志记录、数据清理、发送通知等操作。</p>
     * 
     * @param requestParameter 请求参数
     * @param response 业务处理结果
     * @throws Exception 如果后置处理失败
     */
    protected void applyAfter(T requestParameter, R response) throws Exception {
        // 默认不做任何处理
    }

    /**
     * 异常处理
     * 
     * <p>在发生异常时执行，可以记录异常信息、发送告警、进行补偿操作等。</p>
     * 
     * @param requestParameter 请求参数
     * @param e 发生的异常
     */
    protected void applyAfterException(T requestParameter,  Exception e) {
        // 默认不做任何处理
        log.debug("异常处理回调 - strategy: {}, exception: {}", 
                 this.getClass().getSimpleName(), e.getMessage());
    }

//    /**
//     * 获取策略处理器
//     *
//     * <p>根据请求参数和上下文获取对应的策略处理器。</p>
//     * <p>默认实现返回当前实例，子类可以重写此方法实现复杂的路由逻辑。</p>
//     *
//     * @param requestParameter 请求参数
//     * @param dynamicContext 动态上下文
//     * @return 策略处理器
//     * @throws Exception 如果获取失败
//     */
//    protected StrategyHandler<T, D, R> getHandler(T requestParameter, D dynamicContext) throws Exception {
//        // 默认返回当前实例
//        return (StrategyHandler<T, D, R>) this;
//    }
}
