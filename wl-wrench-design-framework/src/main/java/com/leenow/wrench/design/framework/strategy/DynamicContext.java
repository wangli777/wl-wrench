package com.leenow.wrench.design.framework.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 动态上下文
 * 
 * <p>用于在策略执行过程中存储和传递动态数据。提供类型安全的存取方法，
 * 支持线程安全操作，并实现了 AutoCloseable 接口以便资源清理。</p>
 * 
 * <h3>主要特性：</h3>
 * <ul>
 *     <li>类型安全：使用 TypedValue 包装值和类型信息</li>
 *     <li>线程安全：使用 ThreadLocal 为每个线程提供独立副本</li>
 *     <li>资源清理：实现 AutoCloseable，支持 try-with-resources</li>
 *     <li>迭代支持：可以遍历所有键</li>
 * </ul>
 * 
 * <h3>设计说明：</h3>
 * <p>为什么使用 ThreadLocal 而不是 ConcurrentHashMap？</p>
 * <ul>
 *     <li><strong>更好的线程隔离</strong>：每个线程拥有独立的上下文副本，避免并发冲突</li>
 *     <li><strong>无锁性能</strong>：不需要同步机制，读写操作都是 O(1) 时间复杂度</li>
 *     <li><strong>适合请求场景</strong>：一个请求对应一个线程，请求结束自动清理</li>
 *     <li><strong>内存泄漏防护</strong>：必须手动调用 close() 清理，配合 try-with-resources 使用</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * // 使用 try-with-resources 自动清理
 * try (DynamicContext context = new DynamicContext()) {
 *     // 设置值（带类型信息）
 *     context.setValue("user", new User("张三"), User.class);
 *     context.setValue("amount", 1000, Integer.class);
 *     
 *     // 获取值（类型安全）
 *     User user = context.getValue("user", User.class);
 *     Integer amount = context.getValue("amount", Integer.class);
 *     
 *     // 遍历所有键
 *     for (String key : context.keys()) {
 *         System.out.println("Key: " + key);
 *     }
 * }
 * // 自动调用 close() 清理 ThreadLocal，防止内存泄漏
 * }
 * </pre>
 * 
 * @author WangLi
 * @date 2026/4/15
 * @see AutoCloseable
 */
public class DynamicContext implements AutoCloseable {
    
    /**
     * ThreadLocal 数据存储
     * 
     * <p>使用 ThreadLocal 为每个线程提供独立的 HashMap 副本，确保线程安全。</p>
     * <p>注意：必须在使用后调用 close() 方法清理，防止内存泄漏。</p>
     */
    private static final ThreadLocal<Map<String, TypedValue<?>>> THREAD_LOCAL_DATA = 
        ThreadLocal.withInitial(HashMap::new);

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
        THREAD_LOCAL_DATA.get().put(key, new TypedValue<>(value, clazz));
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
        
        Map<String, TypedValue<?>> dataMap = THREAD_LOCAL_DATA.get();
        TypedValue<?> typedValue = dataMap.get(key);
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
        Map<String, TypedValue<?>> dataMap = THREAD_LOCAL_DATA.get();
        TypedValue<?> typedValue = dataMap.get(key);
        return typedValue != null ? (T) typedValue.getValue() : null;
    }

    /**
     * 检查是否包含指定的 key
     * 
     * @param key 键
     * @return 如果包含返回 true，否则返回 false
     */
    public boolean containsKey(String key) {
        return THREAD_LOCAL_DATA.get().containsKey(key);
    }

    /**
     * 获取所有键的集合
     * 
     * @return 所有键的集合
     */
    public Set<String> keys() {
        return THREAD_LOCAL_DATA.get().keySet();
    }

    /**
     * 检查是否为空
     * 
     * @return 如果没有任何数据返回 true，否则返回 false
     */
    public boolean isEmpty() {
        return THREAD_LOCAL_DATA.get().isEmpty();
    }

    /**
     * 获取数据数量
     * 
     * @return 存储的数据项数量
     */
    public int size() {
        return THREAD_LOCAL_DATA.get().size();
    }

    /**
     * 清空所有数据
     * 
     * <p>释放资源，避免内存泄漏。此方法会清理当前线程的 ThreadLocal 数据。</p>
     */
    public void clear() {
        THREAD_LOCAL_DATA.remove();
    }

    /**
     * 关闭上下文，清理资源
     * 
     * <p>实现 AutoCloseable 接口，支持 try-with-resources 语法（Java 7+）。</p>
     * <p>注意：如果使用 Java 6 或更低版本，需要手动调用 close() 方法。</p>
     */
    @Override
    public void close() {
        clear();
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
