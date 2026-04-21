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
    public static <T extends DynamicContext> T getContext() {
        return (T)CONTEXT_HOLDER.get();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
