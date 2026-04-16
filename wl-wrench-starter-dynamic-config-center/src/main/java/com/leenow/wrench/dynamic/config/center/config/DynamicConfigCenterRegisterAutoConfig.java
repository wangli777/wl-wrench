package com.leenow.wrench.dynamic.config.center.config;

import com.leenow.wrench.dynamic.config.center.config.properties.DynamicConfigCenterProperties;
import com.leenow.wrench.dynamic.config.center.config.properties.DynamicConfigCenterRegisterProperties;
import com.leenow.wrench.dynamic.config.center.domain.model.vo.AttributeVO;
import com.leenow.wrench.dynamic.config.center.domain.service.DynamicConfigCenterService;
import com.leenow.wrench.dynamic.config.center.domain.service.IDynamicConfigCenterService;
import com.leenow.wrench.dynamic.config.center.listener.DynamicConfigCenterAdjustValueListener;
import com.leenow.wrench.dynamic.config.center.types.common.Constants;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 动态配置中心自动配置类
 * 
 * <p>Spring Boot 的自动配置核心类，负责创建和配置动态配置中心所需的所有 Bean。</p>
 * 
 * <h3>工作原理：</h3>
 * <ol>
 *     <li>Spring Boot 启动时扫描到 {@code @Configuration} 注解</li>
 *     <li>读取配置文件中的属性（通过 {@code @EnableConfigurationProperties}）</li>
 *     <li>按顺序创建所有 Bean（通过 {@code @Bean} 方法）</li>
 *     <li>使用 {@code @DependsOn} 确保 Bean 的创建顺序</li>
 * </ol>
 * 
 * <h3>Bean 创建顺序：</h3>
 * <ol>
 *     <li>{@code wlWrenchRedissonClient} - Redis 客户端</li>
 *     <li>{@code dynamicConfigCenterService} - 动态配置服务</li>
 *     <li>{@code dynamicConfigCenterBeanPostProcessor} - Bean 后置处理器</li>
 *     <li>{@code dynamicConfigCenterAdjustValueListener} - Redis 消息监听器</li>
 *     <li>{@code dynamicConfigCenterRedisTopic} - Redis Topic</li>
 * </ol>
 * 
 * <h3>配置示例：</h3>
 * <pre>
 * {@code
 * wl:
 *   wrench:
 *     config:
 *       system: test-system
 *       register:
 *         host: 127.0.0.1
 *         port: 6379
 *         password: yourPassword
 * }
 * </pre>
 * 
 * @author WangLi
 * @date 2026/4/15
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.boot.context.properties.EnableConfigurationProperties
 */
@Configuration
@EnableConfigurationProperties({
    DynamicConfigCenterProperties.class,      // 启用动态配置中心配置
    DynamicConfigCenterRegisterProperties.class  // 启用 Redis 注册配置
})
public class DynamicConfigCenterRegisterAutoConfig {
    
    /**
     * 日志记录器
     * <p>记录配置初始化和异常信息</p>
     */
    private final Logger log = LoggerFactory.getLogger(DynamicConfigCenterRegisterAutoConfig.class);

    /**
     * 创建 Redisson Redis 客户端
     * 
     * <p>配置 Redis 连接参数，包括连接池、超时、重试等。</p>
     * 
     * <h3>配置说明：</h3>
     * <ul>
     *     <li>使用 JsonJacksonCodec 进行 JSON 序列化</li>
     *     <li>单节点 Redis 模式（useSingleServer）</li>
     *     <li>支持密码认证（可选）</li>
     *     <li>完善的连接池管理</li>
     * </ul>
     * 
     * @param properties Redis 注册配置属性
     * @return RedissonClient Redis 客户端实例
     * @throws RuntimeException 如果 Redis 连接失败
     */
    @Bean("wlWrenchRedissonClient")
    public RedissonClient redissonClient(DynamicConfigCenterRegisterProperties properties) {
        try {
            // 创建 Redisson 配置对象
            Config config = new Config();
            // 设置编解码器为 JSON（方便调试和查看）
            config.setCodec(JsonJacksonCodec.INSTANCE);

            // 配置单节点 Redis 服务器
            config.useSingleServer()
                    // Redis 服务器地址，格式：redis://host:port
                    .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                    // Redis 密码（可选）
                    .setPassword(properties.getPassword())
                    // 连接池大小（最大并发连接数）
                    .setConnectionPoolSize(properties.getPoolSize())
                    // 最小空闲连接数（保证有足够的连接可用）
                    .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                    // 空闲连接超时时间（毫秒），超过此时间的空闲连接将被关闭
                    .setIdleConnectionTimeout(properties.getIdleTimeout())
                    // 连接超时时间（毫秒）
                    .setConnectTimeout(properties.getConnectTimeout())
                    // 连接失败后的重试次数
                    .setRetryAttempts(properties.getRetryAttempts())
                    // 两次重试之间的间隔时间（毫秒）
                    .setRetryInterval(properties.getRetryInterval())
                    // Ping 检查间隔时间（毫秒），0 表示不检查
                    .setPingConnectionInterval(properties.getPingInterval())
                    // 是否启用 TCP keepalive 保活机制
                    .setKeepAlive(properties.isKeepAlive());

            // 创建 Redisson 客户端
            RedissonClient redissonClient = Redisson.create(config);

            // 检查客户端是否正常启动
            if (redissonClient.isShutdown()) {
                log.error("wl-wrench，Redis 连接初始化失败 - Host: {}, Port: {}", 
                        properties.getHost(), properties.getPort());
                throw new RuntimeException("Redis 连接初始化失败");
            }

            // 记录初始化成功日志
            log.info("wl-wrench，注册器（redis）链接初始化完成。Host: {}, PoolSize: {}, Status: {}", 
                    properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());

            return redissonClient;
            
        } catch (Exception e) {
            // 记录异常日志并抛出运行时异常
            log.error("wl-wrench，Redis 连接创建异常 - Host: {}, Port: {}, Error: {}", 
                     properties.getHost(), properties.getPort(), e.getMessage(), e);
            throw new RuntimeException("Redis 连接创建失败：" + e.getMessage(), e);
        }
    }

    /**
     * 创建动态配置中心服务
     * 
     * <p>核心服务类，负责配置的注入和动态更新。</p>
     * 
     * @param dynamicConfigCenterProperties 动态配置中心配置属性
     * @param wlWrenchRedissonClient Redis 客户端（依赖上面的 Bean）
     * @return IDynamicConfigCenterService 动态配置服务实例
     */
    @Bean
    @DependsOn("wlWrenchRedissonClient")  // 确保在 Redis 客户端之后创建
    public IDynamicConfigCenterService dynamicConfigCenterService(
            DynamicConfigCenterProperties dynamicConfigCenterProperties, 
            RedissonClient wlWrenchRedissonClient) {
        return new DynamicConfigCenterService(dynamicConfigCenterProperties, wlWrenchRedissonClient);
    }

    /**
     * 创建 Bean 后置处理器
     * 
     * <p>在 Spring Bean 初始化后处理带有 {@code @DCCValue} 注解的字段。</p>
     * 
     * @param dynamicConfigCenterService 动态配置服务
     * @return DynamicConfigCenterBeanPostProcessor Bean 后置处理器实例
     */
    @Bean
    @DependsOn("dynamicConfigCenterService")  // 确保在服务实例之后创建
    DynamicConfigCenterBeanPostProcessor dynamicConfigCenterBeanPostProcessor(
            IDynamicConfigCenterService dynamicConfigCenterService) {
        return new DynamicConfigCenterBeanPostProcessor(dynamicConfigCenterService);
    }

    /**
     * 创建动态配置调整监听器
     * 
     * <p>监听 Redis Topic 中的配置变更消息，并更新对应的字段值。</p>
     * 
     * @param dynamicConfigCenterService 动态配置服务
     * @return DynamicConfigCenterAdjustValueListener 监听器实例
     */
    @Bean
    @DependsOn("dynamicConfigCenterService")  // 确保在服务实例之后创建
    DynamicConfigCenterAdjustValueListener dynamicConfigCenterAdjustValueListener(
            IDynamicConfigCenterService dynamicConfigCenterService) {
        return new DynamicConfigCenterAdjustValueListener(dynamicConfigCenterService);
    }

    /**
     * 创建 Redis Topic 并注册监听器
     * 
     * <p>配置 Redis 发布/订阅模式的 Topic，用于广播配置变更消息。</p>
     * 
     * <h3>工作流程：</h3>
     * <ol>
     *     <li>获取指定系统名称的 Redis Topic</li>
     *     <li>注册监听器，监听配置变更消息</li>
     *     <li>当有配置变更时，监听器会自动处理更新</li>
     * </ol>
     * 
     * @param dynamicConfigCenterProperties 动态配置中心配置属性
     * @param redissonClient Redis 客户端
     * @param dynamicConfigCenterAdjustValueListener 配置变更监听器
     * @return RTopic Redis Topic 实例
     */
    @Bean(name = "dynamicConfigCenterRedisTopic")
    @DependsOn({"wlWrenchRedissonClient", "dynamicConfigCenterAdjustValueListener"})
    public RTopic threadPoolConfigAdjustListener(
            DynamicConfigCenterProperties dynamicConfigCenterProperties,
            RedissonClient redissonClient,
            DynamicConfigCenterAdjustValueListener dynamicConfigCenterAdjustValueListener) {
        
        // 构建 Topic 名称：DYNAMIC_CONFIG_CENTER_REDIS_TOPIC_ + systemName
        RTopic topic = redissonClient.getTopic(Constants.getTopic(dynamicConfigCenterProperties.getSystem()));
        
        // 注册监听器，监听 AttributeVO 类型的消息
        topic.addListener(AttributeVO.class, dynamicConfigCenterAdjustValueListener);
        
        return topic;
    }
}
