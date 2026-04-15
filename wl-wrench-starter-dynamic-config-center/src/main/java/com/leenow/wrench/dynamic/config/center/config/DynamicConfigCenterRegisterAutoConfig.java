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

/**
 * @author: WangLi
 * @date: 2026/4/15 20:46
 * @description:
 */
@Configuration
@EnableConfigurationProperties({DynamicConfigCenterProperties.class,DynamicConfigCenterRegisterProperties.class})
public class DynamicConfigCenterRegisterAutoConfig {

    private final Logger log = LoggerFactory.getLogger(DynamicConfigCenterRegisterAutoConfig.class);

    @Bean("wlWrenchRedissonClient")
    public RedissonClient redissonClient(DynamicConfigCenterRegisterProperties properties) {
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
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
                .setKeepAlive(properties.isKeepAlive())
        ;

        RedissonClient redissonClient = Redisson.create(config);

        log.info("wl-wrench，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());

        return redissonClient;
    }

    @Bean
    public IDynamicConfigCenterService dynamicConfigCenterService(DynamicConfigCenterProperties dynamicConfigCenterProperties, RedissonClient wlWrenchRedissonClient) {
        return new DynamicConfigCenterService(dynamicConfigCenterProperties, wlWrenchRedissonClient);
    }

    @Bean
    DynamicConfigCenterBeanPostProcessor dynamicConfigCenterBeanPostProcessor(IDynamicConfigCenterService dynamicConfigCenterService) {
        return new DynamicConfigCenterBeanPostProcessor(dynamicConfigCenterService);
    }

    @Bean
    DynamicConfigCenterAdjustValueListener  dynamicConfigCenterAdjustValueListener(IDynamicConfigCenterService dynamicConfigCenterService) {
        return new DynamicConfigCenterAdjustValueListener(dynamicConfigCenterService);
    }

    @Bean(name = "dynamicConfigCenterRedisTopic")
    public RTopic threadPoolConfigAdjustListener(DynamicConfigCenterProperties dynamicConfigCenterProperties,
                                                 RedissonClient redissonClient,
                                                 DynamicConfigCenterAdjustValueListener dynamicConfigCenterAdjustValueListener) {
        RTopic topic = redissonClient.getTopic(Constants.getTopic(dynamicConfigCenterProperties.getSystem()));
        topic.addListener(AttributeVO.class, dynamicConfigCenterAdjustValueListener);
        return topic;
    }
}
