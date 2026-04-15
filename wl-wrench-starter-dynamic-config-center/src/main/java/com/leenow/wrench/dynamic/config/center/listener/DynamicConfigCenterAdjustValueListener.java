package com.leenow.wrench.dynamic.config.center.listener;

import com.leenow.wrench.dynamic.config.center.domain.model.vo.AttributeVO;
import com.leenow.wrench.dynamic.config.center.domain.service.IDynamicConfigCenterService;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: WangLi
 * @date: 2026/4/15 21:26
 * @description:
 */
public class DynamicConfigCenterAdjustValueListener implements MessageListener<AttributeVO> {
    private final IDynamicConfigCenterService dynamicConfigCenterService;
    private final Logger log = LoggerFactory.getLogger(DynamicConfigCenterAdjustValueListener.class);

    public DynamicConfigCenterAdjustValueListener(IDynamicConfigCenterService dynamicConfigCenterService) {
        this.dynamicConfigCenterService = dynamicConfigCenterService;
    }

    @Override
    public void onMessage(CharSequence charSequence, AttributeVO attributeVO) {
        try {
            log.info("wl-wrench dcc config attribute:{} value:{}", attributeVO.getAttribute(), attributeVO.getValue());
            dynamicConfigCenterService.adjustAttribute(attributeVO);
        }catch (Exception e){
            log.error("wl-wrench dcc error config attribute:{} value:{}", attributeVO.getAttribute(), attributeVO.getValue(), e);
        }


    }
}
