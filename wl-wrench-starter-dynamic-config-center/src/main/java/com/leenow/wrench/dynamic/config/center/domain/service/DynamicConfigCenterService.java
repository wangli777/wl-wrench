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

public class DynamicConfigCenterService implements IDynamicConfigCenterService {
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
        Class<?> targetClass = bean.getClass();
        Object targetObj = bean;
        if (AopUtils.isAopProxy(bean)) {
            targetClass = AopUtils.getTargetClass(bean);
            Object singletonTarget = AopProxyUtils.getSingletonTarget(bean);
            if (singletonTarget != null) {
                targetObj = singletonTarget;
            }
        }

        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DCCValue.class)) {
                continue;
            }
            DCCValue dccValue = field.getAnnotation(DCCValue.class);
            String value = dccValue.value();
            if (StringUtils.isBlank(value)) {
                throw new RuntimeException(field.getName() + " @DCCValue is not config value config case [isSwitch/isSwitch:1]");
            }

            String[] split = value.split(Constants.SYMBOL_COLON);
            String key = dynamicConfigCenterProperties.getKey(split[0].trim());

            String defaultValue = split.length == 2 ? split[1] : null;

            String setValue = defaultValue;
            try {
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
                Object convertedValue = convertValue(setValue, field.getType());
                field.set(targetObj, convertedValue);
                field.setAccessible(false);
                
                log.info("DCC 配置注入成功 - Key: {}, Value: {}, Type: {}", key, convertedValue, field.getType().getSimpleName());
            } catch (IllegalAccessException e) {
                log.error("DCC 配置注入失败 - Key: {}, Error: {}", key, e.getMessage(), e);
                throw new RuntimeException("DCC 配置注入失败：" + key, e);
            }
            dccBeanGroup.put(key, targetObj);
        }
        return bean;
    }

    @Override
    public void adjustAttribute(AttributeVO attribute) {
        String key = dynamicConfigCenterProperties.getKey(attribute.getAttribute());
        String value = attribute.getValue();

        RBucket<String> bucket = redissonClient.getBucket(key);
        if (!bucket.isExists()) {
            log.warn("DCC 配置不存在，跳过更新 - Key: {}", key);
            return;
        }
        bucket.set(value);

        Object bean = dccBeanGroup.get(key);
        if (bean == null) {
            log.warn("DCC Bean 未找到，跳过更新 - Key: {}", key);
            return;
        }

        Class<?> beanClass = bean.getClass();
        if (AopUtils.isAopProxy(bean)) {
            beanClass = AopUtils.getTargetClass(bean);
        }

        try {
            Field field = beanClass.getDeclaredField(attribute.getAttribute());
            field.setAccessible(true);
            Object convertedValue = convertValue(value, field.getType());
            field.set(bean, convertedValue);
            field.setAccessible(false);

            log.info("DCC 配置动态更新成功 - Key: {}, OldValue: {}, NewValue: {}", 
                    key, bucket.get(), value);

        } catch (Exception e) {
            log.error("DCC 配置动态更新失败 - Key: {}, Error: {}", key, e.getMessage(), e);
            throw new RuntimeException("DCC 配置动态更新失败：" + key, e);
        }
    }

    private Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(value);
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(value);
        } else if (targetType == Float.class || targetType == float.class) {
            return Float.valueOf(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.valueOf(value);
        } else if (targetType == Short.class || targetType == short.class) {
            return Short.valueOf(value);
        } else if (targetType == Byte.class || targetType == byte.class) {
            return Byte.valueOf(value);
        } else if (targetType.isEnum()) {
            return Enum.valueOf((Class<Enum>) targetType, value);
        } else {
            return value;
        }
    }
}
