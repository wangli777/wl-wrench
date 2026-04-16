package com.leenow.wrench.test.strategy.framework.tree.node;

import com.alibaba.fastjson.JSON;
import com.leenow.wrench.design.framework.tree.StrategyHandler;
import com.leenow.wrench.test.strategy.framework.tree.AbstractOrderSupport;
import com.leenow.wrench.test.strategy.framework.tree.OrderContext;
import com.leenow.wrench.test.strategy.framework.tree.OrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemberLevel1Node extends AbstractOrderSupport {
    @Override
    protected String doApply(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        log.info("【级别节点-1】规则决策树 requestParameter:{}",requestParameter);
        return "level1" + JSON.toJSONString(dynamicContext);
    }

    @Override
    public StrategyHandler<OrderRequest, OrderContext, String> getHandler(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
