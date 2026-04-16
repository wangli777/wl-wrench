package com.leenow.wrench.test.strategy.framework.tree;

import com.leenow.wrench.design.framework.tree.StrategyHandler;
import com.leenow.wrench.test.strategy.framework.tree.factory.OrderStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest()
public class DesignFrameworkTest {

    @Resource
    private OrderStrategyFactory orderStrategyFactory;

    @Test
    public void test() throws Exception {
        StrategyHandler<OrderRequest, OrderContext, String> strategyHandler = orderStrategyFactory.strategyHandler();
        OrderRequest orderRequest = OrderRequest.builder()
                .name("wangli")
                .address("长沙市")
                .productId("89324343")
                .quantity(6)
                .build();
        String result = strategyHandler.apply(orderRequest, new OrderContext());

        log.info("测试结果:{}", result);
    }

}
