package com.leenow.wrench.design.framework.tree;


/**
 * @author: WangLi
 * @date: 2026/4/16 13:03
 * @description:
 */
public interface StrategyHandler<T,D extends DynamicContext,R> {

    StrategyHandler DEFAULT_HANDLER = (T, D) -> null;

    default R proceed(T requestParameter,D dynamicContext){
       return null;
   }

    R apply(T requestParameter,D dynamicContext) throws Exception;

}
