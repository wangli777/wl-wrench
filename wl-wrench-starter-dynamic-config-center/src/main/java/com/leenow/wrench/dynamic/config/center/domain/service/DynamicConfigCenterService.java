package com.leenow.wrench.dynamic.config.center.domain.service;

import com.leenow.wrench.dynamic.config.center.config.DynamicConfigCenterProperties;
import com.leenow.wrench.dynamic.config.center.domain.model.vo.AttributeVO;
import com.leenow.wrench.dynamic.config.center.types.annotations.DCCValue;
import com.leenow.wrench.dynamic.config.center.types.common.Constants;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: WangLi
 * @date: 2026/4/15 19:36
 * @description:
 */
public class DynamicConfigCenterService implements IDynamicConfigCenterService{
    private final RedissonClient redissonClient;
    private final DynamicConfigCenterProperties dynamicConfigCenterProperties;

    private final Map<String, Object> dccBeanGroup = new ConcurrentHashMap<>();

    private final Logger log = LoggerFactory.getLogger(DynamicConfigCenterService.class);

    public DynamicConfigCenterService(DynamicConfigCenterProperties dynamicConfigCenterProperties, RedissonClient wlWrenchRedissonClient) {
        this.dynamicConfigCenterProperties = dynamicConfigCenterProperties;
        this.redissonClient = wlWrenchRedissonClient;
    }

    @Override
    public Object proxyObject(Object bean) {
        Class<?> targetClass =bean.getClass();
        Object targetObj = bean;
        if (AopUtils.isAopProxy(bean)) {
            targetClass = AopUtils.getTargetClass(bean);
            targetObj = AopProxyUtils.getSingletonTarget(bean);
        }

        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DCCValue.class)) {
                continue;
            }
            DCCValue dccValue = field.getAnnotation(DCCValue.class);
            String value = dccValue.value();
            if (StringUtils.isBlank(value)) {
                throw new RuntimeException(field.getName() + " @DCCValue is not config value config case 「isSwitch/isSwitch:1」");
            }

            String[] split = value.split(Constants.SYMBOL_COLON);
            String key = dynamicConfigCenterProperties.getKey(split[0].trim());

            String defaultValue = split.length == 2 ? split[1] : null;

            // 设置值
            String setValue = defaultValue;
            try {
                // 如果为空则抛出异常
                if (StringUtils.isBlank(defaultValue)) {
                    throw new RuntimeException("dcc config error " + key + " is not null - 请配置默认值！");
                }
                RBucket<String> bucket = redissonClient.getBucket(key);
                if (bucket.isExists()) {
                    setValue = bucket.get();
                } else {
                    bucket.set(defaultValue);
                }
                field.setAccessible(true);
                field.set(targetObj, setValue);
                field.setAccessible(false);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            dccBeanGroup.put(key, targetObj);
        }
        return bean;
    }

    @Override
    public void adjustAttribute(AttributeVO attribute) {

        // 属性信息
        String key = dynamicConfigCenterProperties.getKey(attribute.getAttribute());
        String value = attribute.getValue();

        // 调整属性值
        RBucket<String> bucket = redissonClient.getBucket(key);
        if (!bucket.isExists()) return;
        bucket.set(value);

        Object object = dccBeanGroup.get(key);
        if (object == null) return;

        Class<?> beanClass = object.getClass();
        // 检查 objBean 是否是代理对象
        if(AopUtils.isAopProxy(object)){
            // 获取代理对象的目标对象
            beanClass =  AopUtils.getTargetClass(object);
        }

        try {
            // 1. getDeclaredField 方法用于获取指定类中声明的所有字段，包括私有字段、受保护字段和公共字段。
            // 2. getField 方法用于获取指定类中的公共字段，即只能获取到公共访问修饰符（public）的字段。
            Field field = beanClass.getDeclaredField(attribute.getAttribute());
            field.setAccessible(true);
            field.set(object, value);
            field.setAccessible(false);

            log.info("DCC 节点监听，动态设置值 {} {}", key, value);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
