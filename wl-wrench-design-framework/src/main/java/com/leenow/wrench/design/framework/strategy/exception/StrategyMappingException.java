package com.leenow.wrench.design.framework.strategy.exception;

/**
 * 策略映射异常
 * 
 * <p>当无法根据请求参数映射到对应的策略处理器时抛出此异常。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
public class StrategyMappingException extends StrategyException {
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public StrategyMappingException(String message) {
        super("STRATEGY_MAPPING_ERROR", message);
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 异常原因
     */
    public StrategyMappingException(String message, Throwable cause) {
        super("STRATEGY_MAPPING_ERROR", message, cause);
    }
}
