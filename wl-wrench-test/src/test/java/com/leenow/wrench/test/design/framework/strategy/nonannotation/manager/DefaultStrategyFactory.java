package com.leenow.wrench.test.design.framework.strategy.nonannotation.manager;

import com.leenow.wrench.design.framework.strategy.StrategyHandler;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderContext;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderRequest;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderResponse;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.node.RootNode;
import org.springframework.stereotype.Service;

@Service
public class DefaultStrategyFactory {

    private final RootNode rootNode;

    public DefaultStrategyFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public StrategyHandler<OrderRequest, OrderContext, OrderResponse> strategyHandler() {
        return rootNode;
    }



}
