package com.leenow.wrench.design.framework.strategy.support;

import com.leenow.wrench.design.framework.strategy.DynamicContext;
import com.leenow.wrench.design.framework.strategy.annotation.Action;
import com.leenow.wrench.design.framework.strategy.annotation.Condition;
import com.leenow.wrench.design.framework.strategy.annotation.Fact;
import com.leenow.wrench.design.framework.strategy.annotation.Strategy;
import com.leenow.wrench.design.framework.strategy.exception.StrategyExecutionException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 注解支持工具类
 * 
 * <p>提供注解的解析和执行功能，包括条件匹配、动作执行、参数注入等。</p>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
public class AnnotationSupport {
    
    /**
     * 检查策略类是否满足条件
     * 
     * <p>扫描策略类中所有标注了 {@link Condition} 注解的方法，并执行它们。</p>
     * <p>如果所有条件方法都返回 true，则认为条件匹配。</p>
     * 
     * @param strategy 策略实例
     * @param request 请求参数
     * @param context 动态上下文
     * @return 如果所有条件都匹配返回 true，否则返回 false
     * @throws StrategyExecutionException 如果条件执行失败
     */
    public static boolean matchCondition(Object strategy, Object request, DynamicContext context) {
        Class<?> clazz = strategy.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(Condition.class)) {
                try {
                    // 准备参数（支持@Fact 注解）
                    Object[] args = prepareArguments(method, request, context);
                    
                    // 执行条件方法
                    method.setAccessible(true);
                    Object result = method.invoke(strategy, args);
                    
                    // 检查返回值
                    if (!(result instanceof Boolean)) {
                        throw new StrategyExecutionException(
                            String.format("@Condition 方法必须返回 boolean 类型：%s.%s", 
                                clazz.getSimpleName(), method.getName()));
                    }
                    
                    // 如果有一个条件不满足，直接返回 false
                    if (!(Boolean) result) {
                        return false;
                    }
                    
                } catch (StrategyExecutionException e) {
                    throw e;
                } catch (Exception e) {
                    throw new StrategyExecutionException(
                        String.format("执行条件方法失败：%s.%s", 
                            clazz.getSimpleName(), method.getName()), e);
                }
            }
        }
        
        // 如果没有@Condition 方法，默认匹配
        return true;
    }
    
    /**
     * 执行策略的动作方法
     * 
     * <p>扫描策略类中所有标注了 {@link Action} 注解的方法，并执行第一个找到的方法。</p>
     * 
     * @param strategy 策略实例
     * @param request 请求参数
     * @param context 动态上下文
     * @param <R> 返回值类型
     * @return 动作执行结果
     * @throws StrategyExecutionException 如果找不到@Action 方法或执行失败
     */
    @SuppressWarnings("unchecked")
    public static <R> R executeAction(Object strategy, Object request, DynamicContext context) {
        Class<?> clazz = strategy.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.isAnnotationPresent(Action.class)) {
                try {
                    // 准备参数（支持@Fact 注解）
                    Object[] args = prepareArguments(method, request, context);
                    
                    // 执行动作方法
                    method.setAccessible(true);
                    return (R) method.invoke(strategy, args);
                    
                } catch (StrategyExecutionException e) {
                    throw e;
                } catch (Exception e) {
                    throw new StrategyExecutionException(
                        String.format("执行动作方法失败：%s.%s", 
                            clazz.getSimpleName(), method.getName()), e);
                }
            }
        }
        
        throw new StrategyExecutionException(
            String.format("未找到@Action 注解的方法：%s", clazz.getSimpleName()));
    }
    
    /**
     * 准备方法参数
     * 
     * <p>解析方法参数，支持 {@link Fact} 注解从上下文中注入值。</p>
     * 
     * @param method 方法
     * @param request 请求参数
     * @param context 动态上下文
     * @return 参数数组
     */
    private static Object[] prepareArguments(Method method, Object request, DynamicContext context) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            
            // 检查是否有@Fact 注解
            if (parameter.isAnnotationPresent(Fact.class)) {
                Fact fact = parameter.getAnnotation(Fact.class);
                String factName = fact.value();
                
                // 从上下文中获取值
                Object value = context.getValue(factName);
                if (value == null) {
                    throw new StrategyExecutionException(
                        String.format("Fact '%s' 在上下文中不存在", factName));
                }
                
                args[i] = value;
                
            } else if (parameter.getType().isInstance(request)) {
                // 如果参数类型与请求类型匹配，直接注入
                args[i] = request;
                
            } else if (parameter.getType() == DynamicContext.class) {
                // 注入上下文
                args[i] = context;
                
            } else {
                // 其他情况注入 null（或者可以抛出异常）
                args[i] = null;
            }
        }
        
        return args;
    }
    
    /**
     * 获取策略注解信息
     * 
     * @param strategy 策略实例
     * @return 策略注解，如果没有则返回 null
     */
    public static Strategy getStrategyAnnotation(Object strategy) {
        Class<?> clazz = strategy.getClass();
        return clazz.getAnnotation(Strategy.class);
    }
    
    /**
     * 获取策略名称
     * 
     * @param strategy 策略实例
     * @return 策略名称，如果没有注解则返回类名
     */
    public static String getStrategyName(Object strategy) {
        Strategy strategyAnnotation = getStrategyAnnotation(strategy);
        if (strategyAnnotation != null) {
            return strategyAnnotation.name();
        }
        return strategy.getClass().getSimpleName();
    }
    
    /**
     * 获取策略优先级
     * 
     * @param strategy 策略实例
     * @return 优先级数值，默认为 0
     */
    public static int getStrategyPriority(Object strategy) {
        Strategy strategyAnnotation = getStrategyAnnotation(strategy);
        if (strategyAnnotation != null) {
            return strategyAnnotation.priority();
        }
        return 0;
    }
}
