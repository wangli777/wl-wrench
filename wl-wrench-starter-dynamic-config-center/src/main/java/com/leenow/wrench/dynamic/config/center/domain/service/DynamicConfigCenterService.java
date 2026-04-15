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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.DisposableBean;

/**
 * 动态配置中心服务实现类
 * 支持多个 Bean、多个字段使用同一个配置 Key
 * 实现 DisposableBean 接口，在 Spring 容器关闭时清理资源
 * 
 * @author: WangLi
 * @date: 2026/4/15 19:36
 */
public class DynamicConfigCenterService implements IDynamicConfigCenterService, DisposableBean {
    
    private final RedissonClient redissonClient;
    private final DynamicConfigCenterProperties dynamicConfigCenterProperties;

    /**
     * 核心数据结构：配置 Key -> 字段信息列表
     * 使用 ConcurrentHashMap 保证线程安全
     * 使用 CopyOnWriteArrayList 保证列表在并发读写时的安全性
     * 
     * 设计说明：
     * 1. 支持多个不同的 Bean 使用同一个配置 Key
     * 2. 支持同一个 Bean 中多个字段使用同一个配置 Key
     * 3. 当配置变更时，会通知所有注册的字段进行更新
     */
    private final Map<String, List<FieldInfo>> dccFieldGroup = new ConcurrentHashMap<>();

    private final Logger log = LoggerFactory.getLogger(DynamicConfigCenterService.class);

    public DynamicConfigCenterService(DynamicConfigCenterProperties dynamicConfigCenterProperties, RedissonClient wlWrenchRedissonClient) {
        this.dynamicConfigCenterProperties = dynamicConfigCenterProperties;
        this.redissonClient = wlWrenchRedissonClient;
    }

    /**
     * 字段信息封装类
     * 用于保存每个使用 @DCCValue 注解的字段的完整信息
     * 
     * 设计目的：
     * 1. 封装 Bean 实例、字段反射对象、字段类型等关键信息
     * 2. 便于后续批量更新时能够快速定位和修改字段值
     * 3. 避免在 Map 中直接存储 Bean 对象导致无法追踪具体字段
     */
    private static class FieldInfo {
        private final Object bean;           // Bean 实例引用（用于定位具体对象）
        private final Field field;           // 字段反射对象（用于修改字段值）
        private final Class<?> fieldType;    // 字段类型（用于类型转换）
        private final String fieldName;      // 字段名称（用于日志记录）
        private final String beanClassName;  // Bean 类名（用于日志记录）
        
        /**
         * 构造方法
         * @param bean Bean 实例
         * @param field 字段反射对象
         */
        public FieldInfo(Object bean, Field field) {
            this.bean = bean;
            this.field = field;
            this.fieldType = field.getType();
            this.fieldName = field.getName();
            this.beanClassName = bean.getClass().getSimpleName();
        }
        
        // ==================== Getter 方法 ====================
        
        public Object getBean() { 
            return bean; 
        }
        
        public Field getField() { 
            return field; 
        }
        
        public Class<?> getFieldType() { 
            return fieldType; 
        }
        
        public String getFieldName() { 
            return fieldName; 
        }
        
        public String getBeanClassName() { 
            return beanClassName; 
        }
        
        /**
         * 重写 toString 方法，便于日志输出
         */
        @Override
        public String toString() {
            return String.format("FieldInfo{bean=%s, field=%s, type=%s}", 
                    beanClassName, fieldName, fieldType.getSimpleName());
        }
    }

    /**
     * 代理对象处理方法
     * 在 Bean 初始化后，扫描并处理所有带有 @DCCValue 注解的字段
     * 
     * 【核心功能】
     * 1. 处理 AOP 代理对象，获取真实的目标对象
     * 2. 扫描所有字段，识别 @DCCValue 注解
     * 3. 从 Redis 读取配置值并注入到字段
     * 4. 将字段信息注册到 dccFieldGroup 中，支持后续动态更新
     * 
     * 【关键改进】
     * - 支持多个 Bean 使用同一个配置 Key
     * - 支持同一个 Bean 中多个字段使用同一个配置 Key
     * - 使用 List 存储所有字段信息，而不是覆盖
     * 
     * @param bean Spring 容器中的 Bean 实例
     * @return 处理后的 Bean 实例
     */
    @Override
    public Object proxyObject(Object bean) {
        // ========== 步骤 1：处理 AOP 代理，获取真实目标对象 ==========
        Class<?> targetClass = bean.getClass();
        Object targetObj = bean;
        
        // 如果 Bean 被 AOP 代理了，需要获取目标类和目标对象
        // 原因：@DCCValue 注解的字段在目标类中，不在代理类中
        if (AopUtils.isAopProxy(bean)) {
            targetClass = AopUtils.getTargetClass(bean);
            Object singletonTarget = AopProxyUtils.getSingletonTarget(bean);
            if (singletonTarget != null) {
                targetObj = singletonTarget;
            }
        }

        // ========== 步骤 2：扫描所有字段，处理 @DCCValue 注解 ==========
        Field[] fields = targetClass.getDeclaredFields();
        for (Field field : fields) {
            // 跳过没有 @DCCValue 注解的字段
            if (!field.isAnnotationPresent(DCCValue.class)) {
                continue;
            }
            
            // 获取注解配置
            DCCValue dccValue = field.getAnnotation(DCCValue.class);
            String valueConfig = dccValue.value();
            
            // 校验配置格式
            if (StringUtils.isBlank(valueConfig)) {
                throw new RuntimeException(field.getName() + 
                    " @DCCValue 必须配置值，格式如 [configKey:defaultValue]");
            }

            // ========== 步骤 3：解析配置 Key 和默认值 ==========
            // 配置格式：configKey:defaultValue
            String[] split = valueConfig.split(Constants.SYMBOL_COLON);
            // 构建完整的配置 Key：systemName_configKey
            String configKey = dynamicConfigCenterProperties.getKey(split[0].trim());
            // 获取默认值（冒号后面的部分）
            String defaultValue = split.length == 2 ? split[1] : null;

            // ========== 步骤 4：从 Redis 获取配置值或设置默认值 ==========
            String setValue = defaultValue;
            try {
                // 校验默认值不能为空
                if (StringUtils.isBlank(defaultValue)) {
                    throw new RuntimeException("DCC 配置 " + configKey + 
                        " 必须设置默认值！");
                }
                
                // 操作 Redis Bucket
                RBucket<String> bucket = redissonClient.getBucket(configKey);
                if (bucket.isExists()) {
                    // Redis 中已有配置，读取现有值
                    setValue = bucket.get();
                    log.debug("DCC 从 Redis 读取配置 - Key: {}, Value: {}", configKey, setValue);
                } else {
                    // Redis 中没有配置，使用默认值
                    bucket.set(defaultValue);
                    log.debug("DCC 设置默认值到 Redis - Key: {}, Value: {}", configKey, defaultValue);
                }
                
                // ========== 步骤 5：类型转换并注入值 ==========
                // 设置字段可访问（因为字段可能是 private 的）
                field.setAccessible(true);
                // 关键：根据字段类型进行类型转换
                // 例如：String "10" -> Integer 10
                Object convertedValue = convertValue(setValue, field.getType());
                // 将转换后的值注入到字段
                field.set(targetObj, convertedValue);
                // 恢复字段访问权限（安全考虑）
                field.setAccessible(false);
                
                // 记录成功日志
                log.info("DCC 配置注入成功 - Key: {}, Bean: {}, Field: {}, Value: {}, Type: {}", 
                        configKey, bean.getClass().getSimpleName(), 
                        field.getName(), convertedValue, field.getType().getSimpleName());
                        
            } catch (IllegalAccessException e) {
                // 记录失败日志
                log.error("DCC 配置注入失败 - Key: {}, Error: {}", configKey, e.getMessage(), e);
                throw new RuntimeException("DCC 配置注入失败：" + configKey, e);
            }
            
            // ========== 步骤 6：注册字段信息到 dccFieldGroup ==========
            // 【关键改进点】使用 List 存储，支持多个字段使用同一个 Key
            // 如果使用 Map 会覆盖之前的字段，导致只有最后一个字段能收到更新通知
            FieldInfo fieldInfo = new FieldInfo(targetObj, field);
            
            // computeIfAbsent：如果 Key 不存在则创建一个新的 CopyOnWriteArrayList
            // add：将当前字段信息添加到列表中
            dccFieldGroup.computeIfAbsent(configKey, k -> new CopyOnWriteArrayList<>())
                         .add(fieldInfo);
            
            // 记录注册信息（调试级别）
            log.debug("DCC 字段注册成功 - Key: {}, 当前注册字段数：{}, FieldInfo: {}", 
                     configKey, dccFieldGroup.get(configKey).size(), fieldInfo);
        }
        
        return bean;
    }

    /**
     * 动态调整属性值方法
     * 当 Redis 中的配置发生变化时，该方法会被调用，更新所有相关字段的值
     * 
     * 【工作流程】
     * 1. 根据配置 Key 找到所有注册的字段
     * 2. 更新 Redis 中的配置值
     * 3. 遍历所有字段，逐个更新值
     * 4. 记录成功/失败统计信息
     * 
     * 【关键特性】
     * - 支持批量更新：一次配置变更，多个字段同时更新
     * - 容错机制：单个字段更新失败不影响其他字段
     * - 详细日志：记录每个字段的更新结果，便于排查问题
     * 
     * @param attribute 属性变更对象（包含属性名和新值）
     */
    @Override
    public void adjustAttribute(AttributeVO attribute) {
        // ========== 步骤 1：构建完整的配置 Key ==========
        String configKey = dynamicConfigCenterProperties.getKey(attribute.getAttribute());
        String newValue = attribute.getValue();

        // ========== 步骤 2：获取所有使用该配置的字段信息 ==========
        List<FieldInfo> fieldInfos = dccFieldGroup.get(configKey);
        
        // 如果没有注册任何字段，记录警告日志并返回
        if (fieldInfos == null || fieldInfos.isEmpty()) {
            log.warn("DCC 配置未找到任何注册字段 - Key: {}, 请检查是否有 Bean 使用 @DCCValue 注解", configKey);
            return;
        }

        // ========== 步骤 3：更新 Redis 中的配置值 ==========
        RBucket<String> bucket = redissonClient.getBucket(configKey);
        if (!bucket.isExists()) {
            log.warn("DCC Redis Bucket 不存在 - Key: {}, 可能是配置未初始化", configKey);
            return;
        }
        bucket.set(newValue);
        log.debug("DCC Redis 配置已更新 - Key: {}, NewValue: {}", configKey, newValue);

        // ========== 步骤 4：遍历所有字段，逐个更新值 ==========
        int successCount = 0;  // 成功计数
        int failCount = 0;     // 失败计数
        
        for (FieldInfo fieldInfo : fieldInfos) {
            try {
                Field field = fieldInfo.getField();
                Object bean = fieldInfo.getBean();
                
                // 设置字段可访问
                field.setAccessible(true);
                // 类型转换：String -> 字段实际类型
                Object convertedValue = convertValue(newValue, fieldInfo.getFieldType());
                // 更新字段值
                field.set(bean, convertedValue);
                // 恢复访问权限
                field.setAccessible(false);
                
                // 成功计数
                successCount++;
                // 记录成功日志
                log.info("DCC 配置更新成功 - Key: {}, Bean: {}, Field: {}, OldValue: {}, NewValue: {}", 
                        configKey, fieldInfo.getBeanClassName(), 
                        fieldInfo.getFieldName(), bucket.get(), newValue);
                        
            } catch (Exception e) {
                // 失败计数
                failCount++;
                // 记录失败日志（包含完整堆栈）
                log.error("DCC 配置更新失败 - Key: {}, Bean: {}, Field: {}, Error: {}", 
                         configKey, fieldInfo.getBeanClassName(), 
                         fieldInfo.getFieldName(), e.getMessage(), e);
            }
        }

        // ========== 步骤 5：记录批量更新的统计信息 ==========
        log.info("DCC 批量更新完成 - Key: {}, 成功：{}/{}, 失败：{}/{}", 
                configKey, successCount, fieldInfos.size(), failCount, fieldInfos.size());
        
        // 如果有失败的，记录警告日志
        if (failCount > 0) {
            log.warn("DCC 批量更新存在失败 - Key: {}, 失败数：{}, 请检查日志详情", configKey, failCount);
        }
    }

    /**
     * 类型转换方法
     * 将 String 类型的配置值转换为目标字段类型
     * 
     * 【支持的类型】
     * 1. 基本包装类型：Integer, Long, Double, Float, Boolean, Short, Byte
     * 2. 基本数据类型：int, long, double, float, boolean, short, byte
     * 3. String 类型：直接返回
     * 4. 枚举类型：通过 Enum.valueOf 转换
     * 5. 其他类型：返回原始 String 值
     * 
     * 【设计考虑】
     * - 支持常用数据类型，满足大部分场景
     * - 枚举类型支持方便状态配置
     * - 其他类型返回 String 保持兼容性
     * 
     * @param value 原始 String 值
     * @param targetType 目标字段类型
     * @return 转换后的对象
     */
    private Object convertValue(String value, Class<?> targetType) {
        // String 类型：直接返回
        if (targetType == String.class) {
            return value;
        } 
        // Integer 类型
        else if (targetType == Integer.class || targetType == int.class) {
            return Integer.valueOf(value);
        } 
        // Long 类型
        else if (targetType == Long.class || targetType == long.class) {
            return Long.valueOf(value);
        } 
        // Double 类型
        else if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(value);
        } 
        // Float 类型
        else if (targetType == Float.class || targetType == float.class) {
            return Float.valueOf(value);
        } 
        // Boolean 类型
        else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.valueOf(value);
        } 
        // Short 类型
        else if (targetType == Short.class || targetType == short.class) {
            return Short.valueOf(value);
        } 
        // Byte 类型
        else if (targetType == Byte.class || targetType == byte.class) {
            return Byte.valueOf(value);
        } 
        // 枚举类型
        else if (targetType.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<Enum> enumClass = (Class<Enum>) targetType;
            return Enum.valueOf(enumClass, value);
        } 
        // 其他类型：返回原始 String 值
        else {
            log.warn("DCC 不支持的类型 [{}]，返回原始 String 值", targetType.getSimpleName());
            return value;
        }
    }

    /**
     * Spring Bean 销毁时的清理方法
     * 实现 DisposableBean 接口，在 Spring 容器关闭时自动调用
     * 
     * 【清理目的】
     * 1. 释放 dccFieldGroup 占用的内存
     * 2. 防止内存泄漏（特别是 Prototype 作用域的 Bean）
     * 3. 记录关闭日志，便于追踪生命周期
     * 
     * 【触发时机】
     * - Spring 容器关闭时
     * - 手动调用 applicationContext.close() 时
     */
    @Override
    public void destroy() throws Exception {
        int totalKeys = dccFieldGroup.size();
        int totalFields = dccFieldGroup.values().stream()
                .mapToInt(List::size)
                .sum();
        
        // 清空所有字段引用
        dccFieldGroup.clear();
        
        log.info("DCC 服务销毁完成 - 清理配置 Key 数：{}, 清理字段总数：{}", totalKeys, totalFields);
    }

    /**
     * 手动清理指定 Bean 的所有字段注册信息
     * 适用于 Prototype 作用域的 Bean，可以在 Bean 销毁时手动调用
     * 
     * 【使用场景】
     * 1. Prototype Bean 销毁时
     * 2. 需要主动释放某个 Bean 的配置监听时
     * 3. 动态卸载模块时
     * 
     * @param bean 需要清理的 Bean 实例
     */
    public void unregisterBean(Object bean) {
        if (bean == null) {
            log.warn("DCC 清理 Bean 失败 - Bean 为 null");
            return;
        }

        int removedCount = 0;
        
        // 遍历所有配置 Key
        for (List<FieldInfo> fieldInfos : dccFieldGroup.values()) {
            // 移除属于该 Bean 的所有字段信息
            removedCount += fieldInfos.removeIf(fieldInfo -> fieldInfo.getBean() == bean) ? 1 : 0;
        }
        
        if (removedCount > 0) {
            log.info("DCC 手动清理 Bean 成功 - Bean: {}, 清理字段数：{}", 
                    bean.getClass().getSimpleName(), removedCount);
        } else {
            log.debug("DCC 手动清理 Bean - Bean: {}, 未找到注册字段", 
                     bean.getClass().getSimpleName());
        }
    }

    /**
     * 获取指定配置 Key 的注册字段数量
     * 用于监控和调试
     * 
     * @param configKey 配置 Key
     * @return 注册的字段数量
     */
    public int getFieldCount(String configKey) {
        List<FieldInfo> fieldInfos = dccFieldGroup.get(configKey);
        return fieldInfos == null ? 0 : fieldInfos.size();
    }

    /**
     * 获取所有配置的注册统计信息
     * 用于监控和调试
     * 
     * @return 统计信息字符串
     */
    public String getStatistics() {
        int totalKeys = dccFieldGroup.size();
        int totalFields = dccFieldGroup.values().stream()
                .mapToInt(List::size)
                .sum();
        
        return String.format("DCC 统计 - 配置 Key 数：%d, 注册字段数：%d", totalKeys, totalFields);
    }
}
