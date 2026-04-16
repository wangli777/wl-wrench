package com.leenow.wrench.dynamic.config.center.config;

import com.leenow.wrench.dynamic.config.center.domain.service.IDynamicConfigCenterService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 动态配置中心 Bean 后置处理器
 * 
 * <p>实现 Spring 的 {@link BeanPostProcessor} 接口，在 Bean 初始化后处理带有 {@link com.leenow.wrench.dynamic.config.center.types.annotations.DCCValue} 
 * 注解的字段。</p>
 * 
 * <h3>工作原理：</h3>
 * <ol>
 *     <li>Spring 容器创建 Bean 实例</li>
 *     <li>调用 BeanPostProcessor.postProcessAfterInitialization() 方法</li>
 *     <li>扫描 Bean 中所有带有 @DCCValue 注解的字段</li>
 *     <li>从 Redis 读取配置值并注入到字段</li>
 *     <li>注册字段信息，用于后续动态更新</li>
 * </ol>
 * 
 * <h3>执行时机：</h3>
 * <p>在 Spring 容器初始化任何 Bean 之后都会调用此处理器。</p>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * @Service
 * public class MyService {
 *     @DCCValue("downgradeSwitch:0")
 *     private String downgradeSwitch;
 * }
 * 
 * // Spring 容器初始化 MyService 后，BeanPostProcessor 会自动处理 @DCCValue 字段
 * }
 * </pre>
 * 
 * @author WangLi
 * @date 2026/4/15 20:17
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @see com.leenow.wrench.dynamic.config.center.domain.service.DynamicConfigCenterService#proxyObject(Object)
 */
public class DynamicConfigCenterBeanPostProcessor implements BeanPostProcessor {
    
    /**
     * 动态配置中心服务接口
     * <p>用于执行实际的配置注入操作</p>
     */
    private final IDynamicConfigCenterService dynamicConfigCenterService;

    public DynamicConfigCenterBeanPostProcessor(IDynamicConfigCenterService dynamicConfigCenterService) {
        this.dynamicConfigCenterService = dynamicConfigCenterService;
    }

    /**
     * Bean 初始化后的处理方法
     * 
     * <p>此方法会在 Spring 容器完成 Bean 的初始化后调用，用于处理带有 @DCCValue 注解的字段。</p>
     * 
     * @param bean 新创建的 Bean 实例
     * @param beanName Bean 的名称
     * @return 处理后的 Bean 实例（通常返回原 Bean）
     * @throws BeansException 如果配置注入失败
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 调用 DynamicConfigCenterService 的 proxyObject 方法处理配置注入
        return dynamicConfigCenterService.proxyObject(bean);
    }
}
