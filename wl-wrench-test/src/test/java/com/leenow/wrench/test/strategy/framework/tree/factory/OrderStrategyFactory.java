package com.leenow.wrench.test.strategy.framework.tree.factory;

import com.leenow.wrench.design.framework.tree.DynamicContext;
import com.leenow.wrench.design.framework.tree.StrategyHandler;
import com.leenow.wrench.test.strategy.framework.tree.OrderContext;
import com.leenow.wrench.test.strategy.framework.tree.OrderRequest;
import com.leenow.wrench.test.strategy.framework.tree.node.RootNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class OrderStrategyFactory {

    private final RootNode rootNode;

    public OrderStrategyFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }

    public StrategyHandler<OrderRequest, OrderContext, String> strategyHandler() {
        return rootNode;
    }


}
