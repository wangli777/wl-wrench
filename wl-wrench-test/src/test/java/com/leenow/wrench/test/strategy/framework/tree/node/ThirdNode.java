package com.leenow.wrench.test.strategy.framework.tree.node;

import com.leenow.wrench.design.framework.tree.StrategyHandler;
import com.leenow.wrench.test.strategy.framework.tree.AbstractOrderSupport;
import com.leenow.wrench.test.strategy.framework.tree.OrderContext;
import com.leenow.wrench.test.strategy.framework.tree.OrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: WangLi
 * @date: 2026/4/16 13:47
 * @description:
 */
@Slf4j
@Component
public class ThirdNode extends AbstractOrderSupport {

    @Autowired
    private MemberLevel1Node memberLevel1Node;

    @Autowired
    private MemberLevel2Node memberLevel2Node;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    protected String applyBefore(OrderRequest requestParameter, OrderContext dynamicContext) {
        if ("1".equals(requestParameter.getName())){
            return "applyBefore";
        }
        return super.applyBefore(requestParameter, dynamicContext);
    }

    @Override
    public String proceed(OrderRequest requestParameter, OrderContext dynamicContext) {

        if ("2".equals(requestParameter.getName())){
            return "proceed";
        }
        return super.proceed(requestParameter, dynamicContext);
    }

    /**
     * 1. 可执行多线程异步操作，尤其在需要大量加载数据的时候非常有用
     * 2. loadContext 在需要的节点就重写，不需要的节点不用处理
     */
    @Override
    protected void loadContext(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        CompletableFuture<String> accountType01 = CompletableFuture.supplyAsync(() -> {
            log.info("异步查询账户标签，账户标签；开户|冻结|止付|可用");
            return new Random().nextBoolean() ? "账户冻结" : "账户可用";
        }, threadPoolExecutor);

        CompletableFuture<String> accountType02 = CompletableFuture.supplyAsync(() -> {
            log.info("异步查询授信数据，拦截|已授信|已降档");
            return new Random().nextBoolean() ? "拦截" : "已授信";
        }, threadPoolExecutor);

        CompletableFuture.allOf(accountType01, accountType02)
                .thenRun(() -> {
                    dynamicContext.setValue("accountType01", accountType01.join());
                    dynamicContext.setValue("accountType02", accountType02.join());
                }).join();
    }

    @Override
    protected String doApply(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        log.info("【ThirdNode】规则决策树 requestParameter:{}", requestParameter);
//                Integer.parseInt("1xxx");

        // 模拟查询用户级别
        int level = new Random().nextInt(2);
        log.info("模拟查询用户级别 level:{}", level);

        dynamicContext.setUserLevel(level);

        return router(requestParameter, dynamicContext);
    }


    @Override
    public StrategyHandler<OrderRequest, OrderContext, String> getHandler(OrderRequest requestParameter, OrderContext dynamicContext) throws Exception {
        String accountType01 = dynamicContext.getValue("accountType01");
        String accountType02 = dynamicContext.getValue("accountType02");

        if ("账户冻结".equals(accountType01)) {
            return memberLevel1Node;
        }

        if ("拦截".equals(accountType02)) {
            return memberLevel1Node;
        }
        int level = dynamicContext.getUserLevel();

        if (level == 1) {
            return memberLevel1Node;
        }

        return memberLevel2Node;
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
