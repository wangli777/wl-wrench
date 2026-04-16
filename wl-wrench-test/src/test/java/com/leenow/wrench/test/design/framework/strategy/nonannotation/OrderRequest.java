package com.leenow.wrench.test.design.framework.strategy.nonannotation;

import com.leenow.wrench.design.framework.strategy.base.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单请求
 * 
 * @author WangLi
 * @date 2026/4/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest extends BaseRequest {
    
    private String userId;
    private Double amount;
    private Integer userLevel;
    private String orderType;
    
}
