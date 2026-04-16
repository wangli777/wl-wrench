package com.leenow.wrench.test.strategy.framework.tree.node;

import com.leenow.wrench.design.framework.tree.AbstractStrategyRouter;
import com.leenow.wrench.design.framework.tree.DynamicContext;
import com.leenow.wrench.design.framework.tree.StrategyHandler;
import com.leenow.wrench.test.strategy.framework.tree.AbstractOrderSupport;
import com.leenow.wrench.test.strategy.framework.tree.OrderContext;
import com.leenow.wrench.test.strategy.framework.tree.OrderRequest;
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
public class RootNode extends AbstractOrderSupport {

    @Autowired
    private SecondNode nextNode;

    @Override
    protected String doApply(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        log.info("【RootNode】规则决策树 userId:{}", requestParameter);
        return router(requestParameter, dynamicContext);
    }


    @Override
    public StrategyHandler<OrderRequest, OrderContext, String> getHandler(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        return nextNode;
    }

    @Override
    protected void applyAfterException(OrderRequest requestParameter, OrderContext dynamicContext, Exception e) {
        log.info("处理异常，applyAfterException - 用于做日志、监控和mq处理" + e.getMessage());
    }

    @Override
    protected void applyAfter(OrderRequest requestParameter, OrderContext dynamicContext, String s) {
        log.info("处理完成，applyAfter - 用于做日志、监控和mq处理");
    }

}
