package com.leenow.wrench.design.framework.strategy.exception;

/**
 * 策略执行异常
 * 
 * <p>在策略执行过程中发生错误时抛出此异常。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
public class StrategyExecutionException extends StrategyException {
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public StrategyExecutionException(String message) {
        super("STRATEGY_EXECUTION_ERROR", message);
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 异常原因
     */
    public StrategyExecutionException(String message, Throwable cause) {
        super("STRATEGY_EXECUTION_ERROR", message, cause);
    }
}
