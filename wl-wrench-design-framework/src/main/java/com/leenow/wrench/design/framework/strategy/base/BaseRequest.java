package com.leenow.wrench.design.framework.strategy.base;

import java.io.Serializable;

/**
 * 请求参数基类
 * 
 * <p>所有请求参数的父类，提供通用的请求属性和方法。</p>
 * 
 * <h3>主要特性：</h3>
 * <ul>
 *     <li>实现 Serializable 接口，支持序列化</li>
 *     <li>提供请求 ID 用于链路追踪</li>
 *     <li>提供时间戳记录请求时间</li>
 *     <li>可扩展自定义属性</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * public class OrderRequest extends BaseRequest {
 *     private Double amount;
 *     private Integer memberLevel;
 *     private String userId;
 *     
 *     // 构造函数、getter、setter...
 * }
 * }
 * </pre>
 * 
 * @author WangLi
 * @date 2026/4/16
 * @see Serializable
 */
public abstract class BaseRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 请求 ID（用于链路追踪）
     */
    private String requestId;
    
    /**
     * 请求时间戳
     */
    private Long timestamp;
    
    /**
     * 默认构造函数
     */
    protected BaseRequest() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 带 requestId 的构造函数
     * 
     * @param requestId 请求 ID
     */
    protected BaseRequest(String requestId) {
        this.requestId = requestId;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 获取请求 ID
     * 
     * @return 请求 ID
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * 设置请求 ID
     * 
     * @param requestId 请求 ID
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    /**
     * 获取请求时间戳
     * 
     * @return 请求时间戳
     */
    public Long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 设置请求时间戳
     * 
     * @param timestamp 请求时间戳
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
