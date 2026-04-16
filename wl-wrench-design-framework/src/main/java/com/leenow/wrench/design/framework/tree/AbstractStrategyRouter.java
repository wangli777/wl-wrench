package com.leenow.wrench.design.framework.tree;

/**
 * @author: WangLi
 * @date: 2026/4/16 13:27
 * @description:
 */
public abstract class AbstractStrategyRouter<T, D extends DynamicContext, R> implements StrategyMapper<T, D, R>, StrategyHandler<T, D, R> {

    protected StrategyHandler<T, D, R> defaultStrategyHandler = StrategyHandler.DEFAULT_HANDLER;

    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        try {
            //1.前置处理
            R applyBefore = applyBefore(requestParameter, dynamicContext);
            if(applyBefore != null) return applyBefore;

            //2.加载上下文数据
            loadContext(requestParameter, dynamicContext);

            //3.处理业务流程
            R r = doApply(requestParameter, dynamicContext);

            //4.后置处理
            applyAfter(requestParameter, dynamicContext, r);

            return r;
        }catch (Exception e){
            //5.异常处理
            applyAfterException(requestParameter, dynamicContext, e);
            throw e;
        }
    }

    public R router(T requestParameter, D dynamicContext) throws Exception {
        StrategyHandler<T, D, R> handler = this.getHandler(requestParameter, dynamicContext);
        if(handler != null)  return handler.apply(requestParameter, dynamicContext);
        return defaultStrategyHandler.apply(requestParameter, dynamicContext);
    }

    protected R applyBefore(T requestParameter, D dynamicContext) {
        return proceed(requestParameter, dynamicContext);
    }

    protected abstract void loadContext(T requestParameter, D dynamicContext) throws Exception ;

    protected abstract R doApply(T requestParameter, D dynamicContext) throws Exception;

    protected void applyAfter(T requestParameter, D dynamicContext, R r) {
    }

    protected void applyAfterException(T requestParameter, D dynamicContext, Exception e) {
    }








}
