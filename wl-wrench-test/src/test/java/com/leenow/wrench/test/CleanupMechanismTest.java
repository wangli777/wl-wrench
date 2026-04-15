package com.leenow.wrench.test;

import com.leenow.wrench.dynamic.config.center.domain.service.DynamicConfigCenterService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 动态配置中心清理机制独立测试类
 * 
 * 为什么需要独立测试类？
 * 1. destroy() 方法会清空所有字段，影响其他测试
 * 2. 需要独立的 Spring 容器来测试清理后的状态
 * 
 * @DirtiesContext 注解的作用：
 * - 告诉 Spring 在这个测试后关闭并重新创建应用上下文
 * - 确保测试之间的完全隔离
 * - 防止共享状态导致的干扰
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CleanupMechanismTest {

    @Autowired
    private DynamicConfigCenterService dccService;

    /**
     * 测试 destroy 方法的清理功能
     * 验证调用 destroy 后所有字段都被清空
     */
    @Test
    @DirtiesContext
    public void test_destroyCleanup() throws Exception {
        log.info("========== 清理机制独立测试：destroy 方法测试 ==========");
        
        // 获取清理前的统计
        String beforeStats = dccService.getStatistics();
        log.info("清理前统计：{}", beforeStats);
        
        int beforeCount = dccService.getFieldCount("test-system_downgradeSwitch");
        log.info("清理前 downgradeSwitch 字段数：{}", beforeCount);
        
        // 验证清理前有字段
        assert beforeCount > 0 : "清理前应该有字段";
        
        // ========== 调用 destroy 方法 ==========
        log.info("调用 destroy() 方法...");
        dccService.destroy();
        log.info("✓ destroy() 方法调用成功");
        
        // 验证清理后字段数为 0
        int afterDestroyCount = dccService.getFieldCount("test-system_downgradeSwitch");
        log.info("清理后 downgradeSwitch 字段数：{}", afterDestroyCount);
        assert afterDestroyCount == 0 : "清理后字段数应该为 0";
        
        // 验证统计信息
        String afterStats = dccService.getStatistics();
        log.info("清理后统计：{}", afterStats);
        assert afterStats.contains("配置 Key 数：0") : "清理后配置 Key 数应该为 0";
        assert afterStats.contains("注册字段数：0") : "清理后注册字段数应该为 0";
        
        log.info("✓ destroy 清理功能测试通过");
        log.info("========== 测试通过 ==========\n");
    }

    /**
     * 测试 unregisterBean 方法
     * 验证可以手动清理指定 Bean
     */
    @Test
    @DirtiesContext
    public void test_unregisterBean() {
        log.info("========== 清理机制独立测试：unregisterBean 方法测试 ==========");
        
        // 测试清理 null Bean
        dccService.unregisterBean(null);
        log.info("✓ unregisterBean(null) 不抛异常");
        
        // 获取清理前统计
        String beforeStats = dccService.getStatistics();
        log.info("清理前统计：{}", beforeStats);
        
        // 注意：由于无法获取测试 Bean 的引用，这里只验证方法不抛异常
        // 实际场景中，unregisterBean 用于 Prototype Bean 的手动清理
        
        log.info("✓ unregisterBean 方法测试通过");
        log.info("========== 测试通过 ==========\n");
    }
}
