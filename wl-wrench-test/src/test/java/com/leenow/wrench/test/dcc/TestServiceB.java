package com.leenow.wrench.test.dcc;

import com.leenow.wrench.dynamic.config.center.types.annotations.DCCValue;
import org.springframework.stereotype.Service;

/**
 * 测试服务类 B
 * 用于测试多 Bean 共享配置场景
 */
@Service
public class TestServiceB {
    
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;
    
    @DCCValue("sharedConfig:shared_default")
    private String sharedConfig;
    
    // Getters
    public String getDowngradeSwitch() {
        return downgradeSwitch;
    }
    
    public String getSharedConfig() {
        return sharedConfig;
    }
}
