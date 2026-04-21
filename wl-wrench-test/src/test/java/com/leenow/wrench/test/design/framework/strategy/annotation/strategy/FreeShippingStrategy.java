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
 * 免运费策略
 * 
 * <p>演示如何使用注解定义一个免运费策略。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
@Component
@Strategy(name = "freeShippingStrategy", description = "免运费策略", priority = 2)
public class FreeShippingStrategy extends AnnotatedStrategyHandler<OrderRequest, DynamicContext, FreeShippingStrategy.ShippingResponse> {
    
    /**
     * 条件匹配方法
     * 
     * <p>订单金额>=200 元或会员等级>=2 时免运费。</p>
     * 
     * @param request 订单请求
     * @return 如果满足免运费条件返回 true，否则返回 false
     */
    @Condition(description = "订单金额>=200 元或会员等级>=2")
    public boolean match(OrderRequest request) {
        return request.getAmount() >= 200 || request.getMemberLevel() >= 2;
    }
    
    /**
     * 动作执行方法
     * 
     * <p>计算免运费优惠。</p>
     * 
     * @param request 订单请求
     * @param userName 从上下文中注入的用户名
     * @param memberLevel 从上下文中注入的会员等级
     * @return 运费响应
     */
    @Action(description = "计算免运费优惠")
    public ShippingResponse apply(OrderRequest request, 
                                  @Fact("userName") String userName,
                                  @Fact("memberLevel") Integer memberLevel) {
        // 计算运费（默认 10 元）
        double shippingFee = 10.0;
        
        // 判断是否免运费
        boolean isFree = request.getAmount() >= 200 || memberLevel >= 2;
        
        // 创建响应
        ShippingResponse response = new ShippingResponse();
        response.setFreeShipping(isFree);
        response.setOriginalFee(shippingFee);
        response.setFinalFee(isFree ? 0 : shippingFee);
        response.setUserName(userName);
        
        if (isFree) {
            response.setMessage(String.format("恭喜用户%s享受免运费优惠，节省%.2f 元", userName, shippingFee));
        } else {
            response.setMessage(String.format("用户%s需要支付运费%.2f 元", userName, shippingFee));
        }
        
        return response;
    }
    
    /**
     * 运费响应类
     */
    public static class ShippingResponse extends BaseResponse {
        private boolean freeShipping;
        private double originalFee;
        private double finalFee;
        private String userName;
        private String message;
        
        public boolean isFreeShipping() {
            return freeShipping;
        }
        
        public void setFreeShipping(boolean freeShipping) {
            this.freeShipping = freeShipping;
        }
        
        public double getOriginalFee() {
            return originalFee;
        }
        
        public void setOriginalFee(double originalFee) {
            this.originalFee = originalFee;
        }
        
        public double getFinalFee() {
            return finalFee;
        }
        
        public void setFinalFee(double finalFee) {
            this.finalFee = finalFee;
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
