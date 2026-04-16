package com.leenow.wrench.test.design.framework.strategy.nonannotation.node;

import com.alibaba.fastjson.JSON;
import com.leenow.wrench.design.framework.strategy.StrategyHandler;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderContext;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderRequest;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderResponse;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.manager.AbstractOrderSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemberLevel1Node extends AbstractOrderSupport {
    @Override
    protected OrderResponse doApply(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        log.info("【级别节点 -1】规则决策树 requestParameter:{}",requestParameter);
        OrderResponse response = new OrderResponse();
        response.setOrderId("level1" + JSON.toJSONString(dynamicContext));
        return response;
    }

    @Override
    public StrategyHandler<OrderRequest, OrderContext, OrderResponse> getNextHandler(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
//        return null; 这里return null和defaultStrategyHandler都行，因为doApply直接返回了response，并没有调用router方法找下一个
        return defaultStrategyHandler;
    }
}
