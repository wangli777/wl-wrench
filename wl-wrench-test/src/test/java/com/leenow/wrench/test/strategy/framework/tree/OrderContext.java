package com.leenow.wrench.test.strategy.framework.tree;

import com.leenow.wrench.design.framework.tree.DynamicContext;

/**
 * @author: WangLi
 * @date: 2026/4/16 13:50
 * @description:
 */
public class OrderContext extends DynamicContext {

    Integer userLevel;

    public OrderContext() {
    }

    public OrderContext(Integer userLevel) {
        this.userLevel = userLevel;
    }

    public Integer getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(Integer userLevel) {
        this.userLevel = userLevel;
    }
}
