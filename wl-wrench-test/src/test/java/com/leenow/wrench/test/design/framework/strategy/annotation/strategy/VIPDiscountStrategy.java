package com.leenow.wrench.test.design.framework.strategy.annotation.strategy;

import com.leenow.wrench.design.framework.strategy.DynamicContext;
import com.leenow.wrench.design.framework.annostrategy.annotation.Action;
import com.leenow.wrench.design.framework.annostrategy.annotation.Condition;
import com.leenow.wrench.design.framework.annostrategy.annotation.Fact;
import com.leenow.wrench.design.framework.annostrategy.annotation.Strategy;
import com.leenow.wrench.design.framework.strategy.base.BaseResponse;
import com.leenow.wrench.design.framework.annostrategy.support.AnnotatedStrategyHandler;
import org.springframework.stereotype.Component;

/**
 * VIP 折扣策略
 * 
 * <p>演示如何使用注解定义一个 VIP 会员折扣策略。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
@Component
@Strategy(name = "vipDiscountStrategy", description = "VIP 会员折扣策略", priority = 1)
public class VIPDiscountStrategy extends AnnotatedStrategyHandler<OrderRequest, DynamicContext, VIPDiscountStrategy.VIPResponse> {
    
    /**
     * 条件匹配方法
     * 
     * <p>会员等级>=1 时享受 VIP 折扣。</p>
     * 
     * @param request 订单请求
     * @return 如果是 VIP 会员返回 true，否则返回 false
     */
    @Condition(description = "会员等级>=1")
    public boolean match(OrderRequest request) {
        return request.getMemberLevel() >= 1;
    }
    
    /**
     * 动作执行方法
     * 
     * <p>根据会员等级计算折扣。</p>
     * 
     * @param request 订单请求
     * @param userName 从上下文中注入的用户名
     * @param memberLevel 从上下文中注入的会员等级
     * @return VIP 响应
     */
    @Action(description = "计算 VIP 会员折扣")
    public VIPResponse apply(OrderRequest request, 
                            @Fact("userName") String userName,
                            @Fact("memberLevel") Integer memberLevel) {
        // 根据会员等级计算折扣
        double discount = calculateDiscount(memberLevel);
        double savedAmount = request.getAmount() * (1 - discount);
        
        // 创建响应
        VIPResponse response = new VIPResponse();
        response.setDiscount(discount);
        response.setSavedAmount(savedAmount);
        response.setMemberLevel(memberLevel);
        response.setUserName(userName);
        response.setMessage(String.format("VIP 用户%s享受%.2f 折优惠，节省%.2f 元", userName, discount, savedAmount));
        
        return response;
    }
    
    /**
     * 根据会员等级计算折扣
     * 
     * @param memberLevel 会员等级
     * @return 折扣率
     */
    private double calculateDiscount(int memberLevel) {
        switch (memberLevel) {
            case 3:
                return 0.6;  // 6 折
            case 2:
                return 0.7;  // 7 折
            case 1:
                return 0.85; // 85 折
            default:
                return 1.0;  // 无折扣
        }
    }
    
    /**
     * VIP 响应类
     */
    public static class VIPResponse extends BaseResponse {
        private double discount;
        private double savedAmount;
        private int memberLevel;
        private String userName;
        private String message;
        
        public double getDiscount() {
            return discount;
        }
        
        public void setDiscount(double discount) {
            this.discount = discount;
        }
        
        public double getSavedAmount() {
            return savedAmount;
        }
        
        public void setSavedAmount(double savedAmount) {
            this.savedAmount = savedAmount;
        }
        
        public int getMemberLevel() {
            return memberLevel;
        }
        
        public void setMemberLevel(int memberLevel) {
            this.memberLevel = memberLevel;
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
