package com.leenow.wrench.test.strategy.framework.tree;

import com.leenow.wrench.design.framework.tree.AbstractStrategyRouter;

/**
 * @author: WangLi
 * @date: 2026/4/16 13:46
 * @description:
 */
public abstract class AbstractOrderSupport extends AbstractStrategyRouter<OrderRequest, OrderContext, String> {

    @Override
    protected void loadContext(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        // 通用的上下文加载逻辑
    }
}
