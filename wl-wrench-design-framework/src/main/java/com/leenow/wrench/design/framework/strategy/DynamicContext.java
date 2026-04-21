package com.leenow.wrench.design.framework.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author WangLi
 * @date 2026/4/15
 */
public class DynamicContext{
    

    // 虽然 DynamicContext 存储在 ThreadLocal 中，单线程内通常是安全的，
    // 但如果某个实现方在 loadContext 中启动了子线程并共享了同一个 Context 引用，HashMap 在并发 put 时可能出现无限循环或数据丢失。
    // 所以使用ConcurrentHashMap
    private final Map<String, TypedValue<?>> extendProperties = new ConcurrentHashMap<>();

    /**
     * 设置值（带类型信息）
     * 
     * <p>将值和类型信息一起存储，确保后续获取时的类型安全。</p>
     * 
     * @param <T> 值的类型
     * @param key 键
     * @param value 值
     * @param clazz 值的类型信息
     * @throws IllegalArgumentException 如果 key 或 clazz 为 null
     */
    public <T> void setValue(String key, T value, Class<T> clazz) {
        if (key == null || clazz == null) {
            throw new IllegalArgumentException("key 和 clazz 不能为 null");
        }
        extendProperties.put(key, new TypedValue<>(value, clazz));
    }

    /**
     * 获取值（类型安全）
     * 
     * <p>根据 key 和类型信息获取值，如果类型不匹配则抛出 ClassCastException。</p>
     * 
     * @param <T> 值的类型
     * @param key 键
     * @param clazz 值的类型信息
     * @return 值，如果不存在则返回 null
     * @throws ClassCastException 如果实际类型与期望类型不匹配
     * @throws IllegalArgumentException 如果 key 或 clazz 为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key, Class<T> clazz) {
        if (key == null || clazz == null) {
            throw new IllegalArgumentException("key 和 clazz 不能为 null");
        }
        
        TypedValue<?> typedValue = extendProperties.get(key);
        if (typedValue == null) {
            return null;
        }
        
        // 类型检查
        if (!clazz.isInstance(typedValue.getValue())) {
            throw new ClassCastException(
                String.format("类型不匹配 - key: %s, 期望类型：%s, 实际类型：%s", 
                    key, clazz.getSimpleName(), 
                    typedValue.getValue().getClass().getSimpleName()));
        }
        
        return (T) typedValue.getValue();
    }

    /**
     * 获取值（不检查类型，已废弃）
     * 
     * <p>为了向后兼容保留此方法，建议使用 {@link #getValue(String, Class)} 替代。</p>
     * 
     * @param <T> 值的类型
     * @param key 键
     * @return 值，如果不存在则返回 null
     * @deprecated 使用 {@link #getValue(String, Class)} 替代
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        TypedValue<?> typedValue = extendProperties.get(key);
        return typedValue != null ? (T) typedValue.getValue() : null;
    }




    /**
     * 类型化值包装类
     * 
     * <p>用于存储值及其类型信息，确保类型安全。</p>
     * 
     * @param <T> 值的类型
     */
    private static class TypedValue<T> {
        
        /**
         * 值对象
         */
        private final T value;
        
        /**
         * 类型信息
         */
        private final Class<T> type;

        /**
         * 构造函数
         * 
         * @param value 值
         * @param type 类型信息
         */
        public TypedValue(T value, Class<T> type) {
            this.value = value;
            this.type = type;
        }

        /**
         * 获取值
         * 
         * @return 值对象
         */
        public T getValue() {
            return value;
        }

        /**
         * 获取类型信息
         * 
         * @return 类型信息
         */
        public Class<T> getType() {
            return type;
        }
    }
}
