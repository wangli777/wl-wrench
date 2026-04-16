package com.leenow.wrench.test.design.framework.strategy.annotation.manager;

import com.leenow.wrench.design.framework.strategy.DynamicContext;
import com.leenow.wrench.design.framework.strategy.StrategyHandler;
import com.leenow.wrench.design.framework.strategy.annotation.Strategy;
import com.leenow.wrench.design.framework.strategy.base.BaseRequest;
import com.leenow.wrench.design.framework.strategy.base.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 策略注册表
 * 
 * <p>管理所有可用的策略，提供策略注册、查找和执行功能。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
@Service
public class StrategyRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(StrategyRegistry.class);
    
    /**
     * 策略列表，按优先级排序
     * <p>使用原始类型以支持不同类型的策略</p>
     */
    @SuppressWarnings("rawtypes")
    private final List<StrategyHandler<BaseRequest, DynamicContext, BaseResponse>> strategies = new ArrayList<>();
    
    /**
     * 注册策略
     * 
     * @param strategy 策略处理器
     */
    @SuppressWarnings("rawtypes")
    public void register(StrategyHandler strategy) {
        strategies.add(strategy);
        // 按优先级排序
        strategies.sort(new Comparator<StrategyHandler>() {
            @Override
            @SuppressWarnings("rawtypes")
            public int compare(StrategyHandler s1, StrategyHandler s2) {
                int priority1 = getStrategyPriority(s1);
                int priority2 = getStrategyPriority(s2);
                return Integer.compare(priority1, priority2);
            }
        });
        log.info("注册策略：{}，优先级：{}", getStrategyName(strategy), getStrategyPriority(strategy));
    }
    
    /**
     * 查找匹配的策略
     * 
     * @param request 请求对象
     * @param context 上下文
     * @return 匹配的策略列表
     * @throws Exception 执行异常
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<StrategyHandler> findMatchingStrategies(
            BaseRequest request, DynamicContext context) throws Exception {
        
        List<StrategyHandler> matchingStrategies = new ArrayList<>();
        
        for (StrategyHandler strategy : strategies) {
            try {
                if (strategy.match(request, context)) {
                    matchingStrategies.add(strategy);
                    log.debug("策略{}匹配成功", getStrategyName(strategy));
                } else {
                    log.debug("策略{}不匹配", getStrategyName(strategy));
                }
            } catch (Exception e) {
                log.warn("策略{}匹配时发生异常：{}", getStrategyName(strategy), e.getMessage());
            }
        }
        
        return matchingStrategies;
    }
    
    /**
     * 执行所有匹配的策略
     * 
     * @param request 请求对象
     * @param context 上下文
     * @return 所有策略的执行结果
     * @throws Exception 执行异常
     */
    @SuppressWarnings("rawtypes")
    public List<BaseResponse> executeAllMatching(BaseRequest request, DynamicContext context) throws Exception {
        List<BaseResponse> results = new ArrayList<>();
        
        List<StrategyHandler> matchingStrategies = 
            findMatchingStrategies(request, context);
        
        for (StrategyHandler strategy : matchingStrategies) {
            try {
                BaseResponse result = strategy.apply(request, context);
                results.add(result);
                log.info("策略{}执行成功", getStrategyName(strategy));
            } catch (Exception e) {
                log.error("策略{}执行失败：{}", getStrategyName(strategy), e.getMessage(), e);
                throw e;
            }
        }
        
        return results;
    }
    
    /**
     * 执行第一个匹配的策略
     * 
     * @param request 请求对象
     * @param context 上下文
     * @return 第一个匹配策略的执行结果
     * @throws Exception 执行异常
     */
    @SuppressWarnings("rawtypes")
    public BaseResponse executeFirstMatching(BaseRequest request, DynamicContext context) throws Exception {
        List<StrategyHandler> matchingStrategies = 
            findMatchingStrategies(request, context);
        
        if (matchingStrategies.isEmpty()) {
            log.warn("没有找到匹配的策略");
            return null;
        }
        
        // 执行优先级最高的策略（已排序）
        StrategyHandler strategy = matchingStrategies.get(0);
        BaseResponse result = strategy.apply(request, context);
        log.info("执行策略{}成功", getStrategyName(strategy));
        
        return result;
    }
    
    /**
     * 获取策略名称
     * 
     * @param strategy 策略对象
     * @return 策略名称
     */
    @SuppressWarnings("rawtypes")
    private String getStrategyName(StrategyHandler strategy) {
        Strategy strategyAnnotation = (Strategy) strategy.getClass().getAnnotation(Strategy.class);
        return strategyAnnotation != null ? strategyAnnotation.name() : strategy.getClass().getSimpleName();
    }
    
    /**
     * 获取策略优先级
     * 
     * @param strategy 策略对象
     * @return 优先级
     */
    @SuppressWarnings("rawtypes")
    private int getStrategyPriority(StrategyHandler strategy) {
        Strategy strategyAnnotation = (Strategy) strategy.getClass().getAnnotation(Strategy.class);
        return strategyAnnotation != null ? strategyAnnotation.priority() : Integer.MAX_VALUE;
    }
    
    /**
     * 获取所有注册的策略
     * 
     * @return 策略列表
     */
    @SuppressWarnings("rawtypes")
    public List<StrategyHandler> getAllStrategies() {
        return Collections.unmodifiableList(strategies);
    }
    
    /**
     * 清空所有策略
     */
    public void clear() {
        strategies.clear();
        log.info("已清空所有策略");
    }
}
