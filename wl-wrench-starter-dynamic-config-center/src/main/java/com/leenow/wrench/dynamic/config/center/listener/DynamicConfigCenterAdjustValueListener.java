package com.leenow.wrench.dynamic.config.center.listener;

import com.leenow.wrench.dynamic.config.center.domain.model.vo.AttributeVO;
import com.leenow.wrench.dynamic.config.center.domain.service.IDynamicConfigCenterService;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 动态配置中心属性调整监听器
 * 
 * <p>实现 Redisson 的 {@link MessageListener} 接口，监听 Redis Topic 中的配置变更消息。</p>
 * 
 * <h3>工作原理：</h3>
 * <ol>
 *     <li>Redis 发布配置更新消息到 Topic</li>
 *     <li>监听器接收到 {@link AttributeVO} 消息</li>
 *     <li>调用 {@link IDynamicConfigCenterService#adjustAttribute(AttributeVO)} 更新字段值</li>
 *     <li>所有注册了该配置的字段都会被更新</li>
 * </ol>
 * 
 * <h3>消息格式：</h3>
 * <pre>
 * {@code
 * {
 *     "attribute": "downgradeSwitch",  // 配置 Key
 *     "value": "8"                     // 新的配置值
 * }
 * }
 * </pre>
 * 
 * <h3>使用示例：</h3>
 * <pre>
 * {@code
 * // 发布配置更新
 * RTopic topic = redissonClient.getTopic("DYNAMIC_CONFIG_CENTER_REDIS_TOPIC_test-system");
 * topic.publish(new AttributeVO("downgradeSwitch", "8"));
 * 
 * // 监听器会自动接收并处理消息
 * }
 * </pre>
 * 
 * @author WangLi
 * @date 2026/4/15 21:26
 * @see org.redisson.api.listener.MessageListener
 * @see com.leenow.wrench.dynamic.config.center.domain.service.DynamicConfigCenterService#adjustAttribute(AttributeVO)
 */
public class DynamicConfigCenterAdjustValueListener implements MessageListener<AttributeVO> {
    
    /**
     * 动态配置中心服务接口
     * <p>用于执行实际的配置更新操作</p>
     */
    private final IDynamicConfigCenterService dynamicConfigCenterService;
    private final Logger log = LoggerFactory.getLogger(DynamicConfigCenterAdjustValueListener.class);

    /**
     * 构造函数
     * 
     * @param dynamicConfigCenterService 动态配置中心服务实例
     */
    public DynamicConfigCenterAdjustValueListener(IDynamicConfigCenterService dynamicConfigCenterService) {
        this.dynamicConfigCenterService = dynamicConfigCenterService;
    }

    /**
     * 接收 Redis 消息并处理配置更新
     * 
     * <p>当 Redis Topic 收到消息时，此方法会被自动调用。</p>
     * 
     * @param charSequence Redis Topic 名称（通常不使用）
     * @param attributeVO 配置变更对象，包含属性名和新值
     */
    @Override
    public void onMessage(CharSequence charSequence, AttributeVO attributeVO) {
        try {
            // 记录接收到的配置变更
            log.info("wl-wrench dcc config onMessage attribute:{} value:{}",
                    attributeVO.getAttribute(), attributeVO.getValue());
            
            // 调用服务方法更新所有相关字段
            dynamicConfigCenterService.adjustAttribute(attributeVO);
            
        } catch (Exception e) {
            // 记录异常信息，包含完整的堆栈跟踪
            log.error("wl-wrench dcc error onMessage config attribute:{} value:{}",
                    attributeVO.getAttribute(), attributeVO.getValue(), e);
        }
    }
}
