package com.leenow.wrench.test;

import com.leenow.wrench.dynamic.config.center.domain.model.vo.AttributeVO;
import com.leenow.wrench.dynamic.config.center.types.annotations.DCCValue;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RTopic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest()
public class ApiTest {

    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    @DCCValue(value = "maxThreads:10")
    private Integer maxThreads;

    @Resource
    private RTopic dynamicConfigCenterRedisTopic;

    @Test
    public void test_get() {
        log.info("测试结果 - downgradeSwitch: {}, maxThreads: {}", downgradeSwitch, maxThreads);
        assert downgradeSwitch != null : "downgradeSwitch 不应为 null";
        assert maxThreads != null : "maxThreads 不应为 null";
    }

    @Test
    public void test_publish() throws InterruptedException {
        AttributeVO attributeVO = new AttributeVO("downgradeSwitch", "9");
        dynamicConfigCenterRedisTopic.publish(attributeVO);
        log.info("已发布配置更新：{}", attributeVO);

        CountDownLatch latch = new CountDownLatch(1);
        boolean awaited = latch.await(5, TimeUnit.SECONDS);
        log.info("等待结果：{}", awaited ? "成功" : "超时");
    }

}
