package com.leenow.wrench.design.framework.annostrategy.manager;

import com.leenow.wrench.design.framework.strategy.DynamicContext;
import com.leenow.wrench.design.framework.annostrategy.StrategyHandler;
import com.leenow.wrench.design.framework.strategy.base.BaseRequest;

/**
 * 策略注册表（纯 Java 实现）
 * 
 * <p>管理所有可用的策略处理器，提供策略注册、查找和执行功能。</p>
 * <p>纯 Java 实现，不依赖任何框架，适用于任何 Java 项目。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * // 纯 Java 使用
 * StrategyRegistry registry = new StrategyRegistry();
 * registry.register(new VipDiscountStrategy());
 * 
 * // Spring 环境手动配置（见文档说明）
 * }
 * </pre>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
public class StrategyRegistry extends AbstractStrategyRegistry {
    
    /**
     * 默认构造函数
     */
    public StrategyRegistry() {
    }
    
    /**
     * 注册策略
     * 
     * @param strategy 策略处理器
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void doRegister(StrategyHandler<?, ?, ?> strategy) {
        strategies.add(strategy);
        log.info("注册策略：{}, 优先级：{}", getStrategyName(strategy), getStrategyPriority(strategy));
    }
    
    /**
     * 判断策略是否匹配
     * 
     * @param strategy 策略处理器
     * @param request 请求对象
     * @param context 上下文
     * @return 是否匹配
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected boolean isMatch(StrategyHandler<?, ?, ?> strategy, Object request, DynamicContext context) {
        try {
            // 使用原始类型调用，避免泛型类型转换问题
            boolean match = ((StrategyHandler) strategy).match((BaseRequest) request, (DynamicContext) context);
            if (match) {
                log.debug("策略 {} 匹配成功", getStrategyName(strategy));
            } else {
                log.debug("策略 {} 不匹配", getStrategyName(strategy));
            }
            return match;
        } catch (Exception e) {
            log.warn("策略 {} 匹配时发生异常：{}", getStrategyName(strategy), e.getMessage());
            return false;
        }
    }
}
