package com.leenow.wrench.design.framework.strategy;



/**
 * 能力上下文ContextHolder
 *
 * @author wangli
 */
public class DynamicContextHolder {

    private final static ThreadLocal<DynamicContext> CONTEXT_HOLDER = new ThreadLocal<>();

    public static <T extends DynamicContext> void setContext( T context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        CONTEXT_HOLDER.set(context);
    }
    /**
     * 获取当前线程的上下文
     *
     * @return 上下文对象，如果不存在则返回 null
     */
    public static DynamicContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
