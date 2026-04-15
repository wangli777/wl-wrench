package com.leenow.wrench.dynamic.config.center.domain.service;

import com.leenow.wrench.dynamic.config.center.domain.model.vo.AttributeVO;

/**
 * @author: WangLi
 * @date: 2026/4/15 19:34
 * @description: 动态配置中心服务接口
 */
public interface IDynamicConfigCenterService {

    /**
     * 在bean对象初始化后，利用BeanPostProcessor的postProcessAfterInitialization方法，为bean对象属性进行动态配置中心的属性注入
     * @param bean
     * @return
     */
    Object proxyObject(Object bean);

    /**
     * 接收redis消息订阅，当redis中的属性发生变化时，调用该方法，调整bean对象的属性值
     * @param attribute
     */
    void adjustAttribute(AttributeVO attribute);
}
