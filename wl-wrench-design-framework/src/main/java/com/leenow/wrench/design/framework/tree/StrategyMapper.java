package com.leenow.wrench.design.framework.tree;

/**
 * @author: WangLi
 * @date: 2026/4/16 13:23
 * @description: 策略映射器
 */
public interface StrategyMapper<T, D extends DynamicContext, R> {

    /**
     * 获取待执行的策略处理器
     * @param requestParameter 入参
     * @param dynamicContext 上下文
     * @return 待执行策略处理器
     * @throws Exception
     */
    StrategyHandler<T, D, R> getHandler(T requestParameter, D dynamicContext) throws Exception;
}
