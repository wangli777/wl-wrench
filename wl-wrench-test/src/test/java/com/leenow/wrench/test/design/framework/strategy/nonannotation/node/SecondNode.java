package com.leenow.wrench.test.design.framework.strategy.nonannotation.node;

import com.leenow.wrench.design.framework.strategy.StrategyHandler;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderContext;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderRequest;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderResponse;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.manager.AbstractOrderSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: WangLi
 * @date: 2026/4/16 13:47
 * @description:
 */
@Slf4j
@Component
public class SecondNode extends AbstractOrderSupport {

    @Autowired
    private ThirdNode nextNode;

    @Override
    protected OrderResponse doApply(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        log.info("【SecondNode】规则决策树 requestParameter:{}", requestParameter);
        return router(requestParameter, dynamicContext);
    }


    @Override
    public StrategyHandler<OrderRequest, OrderContext, OrderResponse> getNextHandler(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        return nextNode;
    }

    @Override
    protected void applyAfterException(OrderRequest requestParameter, OrderContext dynamicContext, Exception e) {
        log.info("处理异常，applyAfterException - 用于做日志、监控和 mq 处理" + e.getMessage());
    }

    @Override
    protected void applyAfter(OrderRequest requestParameter, OrderContext dynamicContext, OrderResponse response) {
        log.info("处理完成，applyAfter - 用于做日志、监控和 mq 处理");
    }

//    @Override
//    protected String applyBefore(OrderRequest requestParameter, OrderContext dynamicContext) {
//        return super.applyBefore(requestParameter, dynamicContext);
//    }
}
