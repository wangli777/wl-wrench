package com.leenow.wrench.dynamic.config.center.config;

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

@Configuration
@EnableConfigurationProperties({DynamicConfigCenterProperties.class,DynamicConfigCenterRegisterProperties.class})
public class DynamicConfigCenterRegisterAutoConfig {

    private final Logger log = LoggerFactory.getLogger(DynamicConfigCenterRegisterAutoConfig.class);

    @Bean("wlWrenchRedissonClient")
    public RedissonClient redissonClient(DynamicConfigCenterRegisterProperties properties) {
        try {
            Config config = new Config();
            config.setCodec(JsonJacksonCodec.INSTANCE);

            config.useSingleServer()
                    .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                    .setPassword(properties.getPassword())
                    .setConnectionPoolSize(properties.getPoolSize())
                    .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                    .setIdleConnectionTimeout(properties.getIdleTimeout())
                    .setConnectTimeout(properties.getConnectTimeout())
                    .setRetryAttempts(properties.getRetryAttempts())
                    .setRetryInterval(properties.getRetryInterval())
                    .setPingConnectionInterval(properties.getPingInterval())
                    .setKeepAlive(properties.isKeepAlive());

            RedissonClient redissonClient = Redisson.create(config);

            if (redissonClient.isShutdown()) {
                log.error("wl-wrench，Redis 连接初始化失败 - Host: {}, Port: {}", properties.getHost(), properties.getPort());
                throw new RuntimeException("Redis 连接初始化失败");
            }

            log.info("wl-wrench，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());

            return redissonClient;
        } catch (Exception e) {
            log.error("wl-wrench，Redis 连接创建异常 - Host: {}, Port: {}, Error: {}", 
                     properties.getHost(), properties.getPort(), e.getMessage(), e);
            throw new RuntimeException("Redis 连接创建失败：" + e.getMessage(), e);
        }
    }

    @Bean
    @DependsOn("wlWrenchRedissonClient")
    public IDynamicConfigCenterService dynamicConfigCenterService(DynamicConfigCenterProperties dynamicConfigCenterProperties, RedissonClient wlWrenchRedissonClient) {
        return new DynamicConfigCenterService(dynamicConfigCenterProperties, wlWrenchRedissonClient);
    }

    @Bean
    @DependsOn("dynamicConfigCenterService")
    DynamicConfigCenterBeanPostProcessor dynamicConfigCenterBeanPostProcessor(IDynamicConfigCenterService dynamicConfigCenterService) {
        return new DynamicConfigCenterBeanPostProcessor(dynamicConfigCenterService);
    }

    @Bean
    @DependsOn("dynamicConfigCenterService")
    DynamicConfigCenterAdjustValueListener  dynamicConfigCenterAdjustValueListener(IDynamicConfigCenterService dynamicConfigCenterService) {
        return new DynamicConfigCenterAdjustValueListener(dynamicConfigCenterService);
    }

    @Bean(name = "dynamicConfigCenterRedisTopic")
    @DependsOn({"wlWrenchRedissonClient", "dynamicConfigCenterAdjustValueListener"})
    public RTopic threadPoolConfigAdjustListener(DynamicConfigCenterProperties dynamicConfigCenterProperties,
                                                 RedissonClient redissonClient,
                                                 DynamicConfigCenterAdjustValueListener dynamicConfigCenterAdjustValueListener) {
        RTopic topic = redissonClient.getTopic(Constants.getTopic(dynamicConfigCenterProperties.getSystem()));
        topic.addListener(AttributeVO.class, dynamicConfigCenterAdjustValueListener);
        return topic;
    }
}
