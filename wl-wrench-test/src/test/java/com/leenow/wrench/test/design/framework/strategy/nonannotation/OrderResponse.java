package com.leenow.wrench.test.design.framework.strategy.nonannotation;

import com.leenow.wrench.design.framework.strategy.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单响应
 * 
 * @author WangLi
 * @date 2026/4/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse extends BaseResponse {
    
    private String orderId;

}
