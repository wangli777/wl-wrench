package com.leenow.wrench.dynamic.config.center.config;

import com.leenow.wrench.dynamic.config.center.domain.service.IDynamicConfigCenterService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author: WangLi
 * @date: 2026/4/15 20:17
 * @description:
 */
public class DynamicConfigCenterBeanPostProcessor implements BeanPostProcessor {
    private final IDynamicConfigCenterService dynamicConfigCenterService;

    public DynamicConfigCenterBeanPostProcessor(IDynamicConfigCenterService dynamicConfigCenterService) {
        this.dynamicConfigCenterService = dynamicConfigCenterService;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return dynamicConfigCenterService.proxyObject(bean);
    }
}
