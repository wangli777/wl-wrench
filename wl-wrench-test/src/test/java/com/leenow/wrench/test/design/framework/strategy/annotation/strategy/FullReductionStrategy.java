package com.leenow.wrench.test.design.framework.strategy.annotation.strategy;

import com.leenow.wrench.design.framework.strategy.DynamicContext;
import com.leenow.wrench.design.framework.strategy.annotation.Action;
import com.leenow.wrench.design.framework.strategy.annotation.Condition;
import com.leenow.wrench.design.framework.strategy.annotation.Fact;
import com.leenow.wrench.design.framework.strategy.annotation.Strategy;
import com.leenow.wrench.design.framework.strategy.base.BaseResponse;
import com.leenow.wrench.design.framework.strategy.support.AnnotatedStrategyHandler;
import org.springframework.stereotype.Component;

/**
 * 满减策略
 * 
 * <p>演示如何使用注解定义一个满减策略。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
@Component
@Strategy(name = "fullReductionStrategy", description = "满减策略", priority = 3)
public class FullReductionStrategy extends AnnotatedStrategyHandler<OrderRequest, DynamicContext, FullReductionStrategy.ReductionResponse> {
    
    /**
     * 条件匹配方法
     * 
     * <p>订单金额>=300 元时享受满减优惠。</p>
     * 
     * @param request 订单请求
     * @return 如果满足满减条件返回 true，否则返回 false
     */
    @Condition(description = "订单金额>=300 元")
    public boolean match(OrderRequest request) {
        return request.getAmount() >= 300;
    }
    
    /**
     * 动作执行方法
     * 
     * <p>计算满减优惠金额。</p>
     * 
     * @param request 订单请求
     * @param userName 从上下文中注入的用户名
     * @return 满减响应
     */
    @Action(description = "计算满减优惠")
    public ReductionResponse apply(OrderRequest request, 
                                   @Fact("userName") String userName) {
        // 计算满减金额
        double reductionAmount = calculateReduction(request.getAmount());
        
        // 创建响应
        ReductionResponse response = new ReductionResponse();
        response.setReductionAmount(reductionAmount);
        response.setFinalAmount(request.getAmount() - reductionAmount);
        response.setUserName(userName);
        response.setMessage(String.format("用户%s享受满减优惠，减免%.2f 元，最终支付%.2f 元", 
                                         userName, reductionAmount, request.getAmount() - reductionAmount));
        
        return response;
    }
    
    /**
     * 计算满减金额
     * 
     * @param amount 订单金额
     * @return 减免金额
     */
    private double calculateReduction(double amount) {
        if (amount >= 1000) {
            return 200;  // 满 1000 减 200
        } else if (amount >= 500) {
            return 80;   // 满 500 减 80
        } else {
            return 30;   // 满 300 减 30
        }
    }
    
    /**
     * 满减响应类
     */
    public static class ReductionResponse extends BaseResponse {
        private double reductionAmount;
        private double finalAmount;
        private String userName;
        private String message;
        
        public double getReductionAmount() {
            return reductionAmount;
        }
        
        public void setReductionAmount(double reductionAmount) {
            this.reductionAmount = reductionAmount;
        }
        
        public double getFinalAmount() {
            return finalAmount;
        }
        
        public void setFinalAmount(double finalAmount) {
            this.finalAmount = finalAmount;
        }
        
        public String getUserName() {
            return userName;
        }
        
        public void setUserName(String userName) {
            this.userName = userName;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
}
