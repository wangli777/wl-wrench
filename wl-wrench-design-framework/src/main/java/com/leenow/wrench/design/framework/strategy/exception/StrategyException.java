package com.leenow.wrench.design.framework.strategy.exception;

/**
 * 策略执行异常基类
 * 
 * <p>所有策略相关的异常都继承自此类。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
public class StrategyException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public StrategyException(String message) {
        super(message);
        this.errorCode = "STRATEGY_ERROR";
    }

    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 异常原因
     */
    public StrategyException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "STRATEGY_ERROR";
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 异常消息
     */
    public StrategyException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数
     * 
     * @param errorCode 错误码
     * @param message 异常消息
     * @param cause 异常原因
     */
    public StrategyException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
}
