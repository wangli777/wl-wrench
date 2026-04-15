package com.leenow.wrench.test;

import com.leenow.wrench.dynamic.config.center.types.annotations.DCCValue;
import org.springframework.stereotype.Service;

/**
 * 测试服务类 A
 * 用于测试多 Bean 共享配置和同 Bean 多字段场景
 */
@Service
public class TestServiceA {
    
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;
    
    @DCCValue("sharedField:default_value")
    private String field1;
    
    @DCCValue("sharedField:default_value")
    private String field2;
    
    @DCCValue("timeout:5000")
    private Long timeout;
    
    @DCCValue("enabled:true")
    private Boolean enabled;
    
    @DCCValue("threshold:0.5")
    private Double threshold;
    
    @DCCValue("mode:MODE_A")
    private Mode mode;
    
    // Getters
    public String getDowngradeSwitch() {
        return downgradeSwitch;
    }
    
    public String getField1() {
        return field1;
    }
    
    public String getField2() {
        return field2;
    }
    
    public Long getTimeout() {
        return timeout;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public Double getThreshold() {
        return threshold;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    // 枚举定义
    public enum Mode {
        MODE_A,
        MODE_B,
        MODE_C
    }
}
