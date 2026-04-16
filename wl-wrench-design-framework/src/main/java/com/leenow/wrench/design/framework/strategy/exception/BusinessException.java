package com.leenow.wrench.design.framework.strategy.exception;

/**
 * 业务异常
 * 
 * <p>在业务逻辑处理过程中发生的异常，通常不需要记录完整堆栈。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
public class BusinessException extends StrategyException {
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public BusinessException(String message) {
        super("BUSINESS_ERROR", message);
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super("BUSINESS_ERROR", message, cause);
    }
}
