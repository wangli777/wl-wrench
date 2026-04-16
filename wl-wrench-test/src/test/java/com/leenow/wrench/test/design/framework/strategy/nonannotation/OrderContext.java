package com.leenow.wrench.test.design.framework.strategy.nonannotation;

import com.leenow.wrench.design.framework.strategy.DynamicContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单上下文
 * 
 * @author WangLi
 * @date 2026/4/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderContext extends DynamicContext {
    
    private String userId;
    private Integer userLevel;
    private String orderChannel;
    
}
