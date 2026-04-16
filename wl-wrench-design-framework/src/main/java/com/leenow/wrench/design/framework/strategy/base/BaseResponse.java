package com.leenow.wrench.design.framework.strategy.base;

import java.io.Serializable;

/**
 * 响应参数基类
 * 
 * <p>所有响应参数的父类，提供通用的响应属性和方法。</p>
 * 
 * <h3>主要特性：</h3>
 * <ul>
 *     <li>实现 Serializable 接口，支持序列化</li>
 *     <li>提供响应 ID 用于链路追踪</li>
 *     <li>提供响应时间记录响应时间</li>
 *     <li>可扩展自定义属性</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * public class OrderResponse extends BaseResponse {
 *     private Double discount;
 *     private String message;
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
public abstract class BaseResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应 ID（用于链路追踪，通常与请求 ID 对应）
     */
    private String responseId;
    
    /**
     * 响应时间
     */
    private Long responseTime;
    
    /**
     * 默认构造函数
     */
    protected BaseResponse() {
        this.responseTime = System.currentTimeMillis();
    }
    
    /**
     * 带 responseId 的构造函数
     * 
     * @param responseId 响应 ID
     */
    protected BaseResponse(String responseId) {
        this.responseId = responseId;
        this.responseTime = System.currentTimeMillis();
    }
    
    /**
     * 获取响应 ID
     * 
     * @return 响应 ID
     */
    public String getResponseId() {
        return responseId;
    }
    
    /**
     * 设置响应 ID
     * 
     * @param responseId 响应 ID
     */
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }
    
    /**
     * 获取响应时间
     * 
     * @return 响应时间
     */
    public Long getResponseTime() {
        return responseTime;
    }
    
    /**
     * 设置响应时间
     * 
     * @param responseTime 响应时间
     */
    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }
}
