package com.leenow.wrench.design.framework.annostrategy.manager;

import com.leenow.wrench.design.framework.strategy.DynamicContext;
import com.leenow.wrench.design.framework.annostrategy.StrategyHandler;
import com.leenow.wrench.design.framework.annostrategy.annotation.Strategy;
import com.leenow.wrench.design.framework.strategy.base.BaseRequest;
import com.leenow.wrench.design.framework.strategy.base.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 抽象策略注册表
 * 
 * <p>提供通用的策略管理逻辑，包括注册、排序、查找、执行等。</p>
 * <p>纯 Java 实现，不依赖任何框架。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
public abstract class AbstractStrategyRegistry {
    
    protected static final Logger log = LoggerFactory.getLogger(AbstractStrategyRegistry.class);
    
    /**
     * 策略存储（使用原始类型以支持多种策略）
     */
    protected final List<StrategyHandler<?, ?, ?>> strategies = new ArrayList<>();
    
    /**
     * 注册策略
     * 
     * @param strategy 策略处理器
     */
    public void register(StrategyHandler<?, ?, ?> strategy) {
        doRegister(strategy);
        sortStrategies();
    }
    
    /**
     * 批量注册策略
     * 
     * @param strategies 策略处理器列表
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registerAll(List<? extends StrategyHandler<?, ?, ?>> strategies) {
        for (StrategyHandler strategy : strategies) {
            register(strategy);
        }
    }


    
    /**
     * 查找匹配的策略
     * 
     * @param request 请求对象
     * @param context 上下文
     * @return 匹配的策略列表
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<StrategyHandler> findMatchingStrategies(Object request, DynamicContext context) {
        List<StrategyHandler> matchingStrategies = new ArrayList<>();
        for (StrategyHandler strategy : strategies) {
            if (isMatch(strategy, request, context)) {
                matchingStrategies.add(strategy);
            }
        }
        return matchingStrategies;
    }
    
    /**
     * 泛型方法：查找匹配的策略（类型安全版本）
     * 
     * @param request 请求对象
     * @param context 上下文
     * @param <T> 请求类型（必须 extends BaseRequest）
     * @return 匹配的策略列表
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseRequest> List<StrategyHandler<T, ?, ?>> findMatchingStrategies(T request, DynamicContext context) {
        return (List<StrategyHandler<T, ?, ?>>) (List<?>) findMatchingStrategies((Object) request, context);
    }
    
    /**
     * 执行所有匹配的策略（泛型方法）
     * 
     * @param request 请求对象
     * @param context 上下文
     * @param <T> 请求类型（必须 extends BaseRequest）
     * @param <R> 响应类型（必须 extends BaseResponse）
     * @return 所有策略的执行结果
     * @throws Exception 执行异常
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends BaseRequest, R extends BaseResponse> List<R> executeAllMatching(T request, DynamicContext context) throws Exception {
        List<R> results = new ArrayList<>();
        
        // 使用原始类型调用，避免泛型问题
        List<StrategyHandler> matchingStrategies = (List<StrategyHandler>) (List<?>) findMatchingStrategies((BaseRequest) request, context);
        
        for (StrategyHandler strategy : matchingStrategies) {
            try {
                BaseResponse result = strategy.handle((BaseRequest) request, context);
                results.add((R) result);
                log.info("策略{}执行成功", getStrategyName(strategy));
            } catch (Exception e) {
                log.error("策略{}执行失败：{}", getStrategyName(strategy), e.getMessage(), e);
                throw e;
            }
        }
        
        return results;
    }
    
    /**
     * 执行第一个匹配的策略（泛型方法）
     * 
     * @param request 请求对象
     * @param context 上下文
     * @param <T> 请求类型（必须 extends BaseRequest）
     * @param <R> 响应类型（必须 extends BaseResponse）
     * @return 第一个匹配策略的执行结果
     * @throws Exception 执行异常
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends BaseRequest, R extends BaseResponse> R executeFirstMatching(T request, DynamicContext context) throws Exception {
        // 使用原始类型调用，避免泛型问题
        List<StrategyHandler> matchingStrategies = (List<StrategyHandler>) (List<?>) findMatchingStrategies((BaseRequest) request, context);
        
        if (matchingStrategies.isEmpty()) {
            log.warn("没有找到匹配的策略");
            return null;
        }
        
        StrategyHandler strategy = matchingStrategies.get(0);
        // 使用原始类型调用，避免泛型类型转换问题
        BaseResponse result = ((StrategyHandler) strategy).handle((BaseRequest) request, context);
        log.info("执行策略 {} 成功", getStrategyName(strategy));
        
        return (R) result;
    }
    
    /**
     * 子类实现：如何注册策略
     * 
     * @param strategy 策略处理器
     */
    protected abstract void doRegister(StrategyHandler<?, ?, ?> strategy);
    
    /**
     * 子类实现：如何判断策略匹配
     * 
     * @param strategy 策略处理器
     * @param request 请求对象
     * @param context 上下文
     * @return 是否匹配
     */
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
    
    /**
     * 排序策略（基于 @Strategy 注解的 priority）
     */
    protected void sortStrategies() {
        strategies.sort(Comparator.comparingInt(this::getStrategyPriority));
    }
    
    /**
     * 获取策略优先级
     * 
     * @param strategy 策略处理器
     * @return 优先级数值
     */
    protected int getStrategyPriority(StrategyHandler<?, ?, ?> strategy) {
        Strategy annotation = strategy.getClass().getAnnotation(Strategy.class);
        return annotation != null ? annotation.priority() : Integer.MAX_VALUE;
    }
    
    /**
     * 获取策略名称
     * 
     * @param strategy 策略处理器
     * @return 策略名称
     */
    protected String getStrategyName(StrategyHandler<?, ?, ?> strategy) {
        Strategy annotation = strategy.getClass().getAnnotation(Strategy.class);
        return annotation != null ? annotation.name() : strategy.getClass().getSimpleName();
    }
    
    /**
     * 获取所有注册的策略
     * 
     * @return 策略列表
     */
    public List<StrategyHandler<?, ?, ?>> getAllStrategies() {
        return Collections.unmodifiableList(strategies);
    }
    
    /**
     * 获取注册的策略数量
     * 
     * @return 策略数量
     */
    public int getStrategyCount() {
        return strategies.size();
    }
    
    /**
     * 清空所有策略
     */
    public void clear() {
        strategies.clear();
        log.info("已清空所有策略");
    }
}
