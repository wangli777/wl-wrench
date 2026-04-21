package com.leenow.wrench.test.design.framework.strategy.nonannotation.manager;



import com.leenow.wrench.design.framework.strategy.AbstractStrategyRouter;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderContext;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderRequest;
import com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderResponse;


public abstract class AbstractOrderSupport extends AbstractStrategyRouter<OrderRequest, OrderContext, OrderResponse> {

//    @Override
//    protected void loadContext(com.leenow.wrench.test.design.framework.strategy.nonannotation.OrderRequest requestParameter) throws Exception {
//        super.loadContext(requestParameter);
//    }

}
