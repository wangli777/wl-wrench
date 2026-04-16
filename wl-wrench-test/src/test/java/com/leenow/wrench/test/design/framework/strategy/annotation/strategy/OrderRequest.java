package com.leenow.wrench.test.design.framework.strategy.annotation.strategy;

import com.leenow.wrench.design.framework.strategy.base.BaseRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 订单请求类
 * 
 * <p>继承统一的请求参数基类，用于订单相关策略。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderRequest extends BaseRequest {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 订单金额
     */
    private Double amount;
    
    /**
     * 会员等级
     */
    private Integer memberLevel;
    
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 带参数构造函数
     * 
     * @param amount 订单金额
     * @param memberLevel 会员等级
     * @param userId 用户 ID
     */
    @Builder
    public OrderRequest(Double amount, Integer memberLevel, String userId) {
        super();
        this.amount = amount;
        this.memberLevel = memberLevel;
        this.userId = userId;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public Integer getMemberLevel() {
        return memberLevel;
    }
    
    public void setMemberLevel(Integer memberLevel) {
        this.memberLevel = memberLevel;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
