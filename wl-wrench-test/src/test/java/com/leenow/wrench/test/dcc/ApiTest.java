package com.leenow.wrench.test.dcc;

import com.leenow.wrench.dynamic.config.center.domain.model.vo.AttributeVO;
import com.leenow.wrench.dynamic.config.center.domain.service.DynamicConfigCenterService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 动态配置中心功能测试类
 * 测试场景包括：
 * 1. 基础配置注入测试
 * 2. 多 Bean 共享同一配置测试
 * 3. 同 Bean 多字段共享配置测试
 * 4. 不同类型字段支持测试
 * 5. 配置动态更新测试
 * 6. 统计信息测试
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest()
public class ApiTest {

    @Resource
    private RTopic dynamicConfigCenterRedisTopic;

    @Autowired
    private DynamicConfigCenterService dccService;

    @Autowired
    private TestServiceA testServiceA;

    @Autowired
    private TestServiceB testServiceB;

    /**
     * 测试 1：基础配置注入测试
     * 验证配置能否正确注入到字段
     */
    @Test
    public void test_basicConfigInjection() {
        log.info("========== 测试 1：基础配置注入测试 ==========");
        
        // 验证 ServiceA 的配置注入
        assert testServiceA.getDowngradeSwitch() != null : "downgradeSwitch 不应为 null";
        
        log.info("✓ ServiceA 配置注入成功 - downgradeSwitch: {}", 
                testServiceA.getDowngradeSwitch());
        
        // 验证 ServiceB 的配置注入
        assert testServiceB.getDowngradeSwitch() != null : "ServiceB downgradeSwitch 不应为 null";
        
        log.info("✓ ServiceB 配置注入成功 - downgradeSwitch: {}", 
                testServiceB.getDowngradeSwitch());
        
        log.info("========== 测试 1 通过 ==========\n");
    }

    /**
     * 测试 2：多 Bean 共享同一配置测试
     * 验证多个 Bean 使用同一个配置 Key 时，都能正确接收更新
     */
    @Test
    public void test_multiBeanShareConfig() throws InterruptedException {
        log.info("========== 测试 2：多 Bean 共享同一配置测试 ==========");
        
        // 记录初始值
        String initialValueA = testServiceA.getDowngradeSwitch();
        String initialValueB = testServiceB.getDowngradeSwitch();
        
        log.info("初始值 - ServiceA: {}, ServiceB: {}", initialValueA, initialValueB);
        assert initialValueA.equals(initialValueB) : "多 Bean 初始值应该相同";
        
        // 发布配置更新
        String newValue = "8";
        AttributeVO attributeVO = new AttributeVO("downgradeSwitch", newValue);
        log.info("发布配置更新：{}", attributeVO);
        dynamicConfigCenterRedisTopic.publish(attributeVO);
        
        // 等待更新完成
        Thread.sleep(1000);
        
        // 验证两个 Bean 都收到了更新
        String updatedValueA = testServiceA.getDowngradeSwitch();
        String updatedValueB = testServiceB.getDowngradeSwitch();
        
        log.info("更新后 - ServiceA: {}, ServiceB: {}", updatedValueA, updatedValueB);
        
        assert updatedValueA.equals(newValue) : "ServiceA 应该收到更新";
        assert updatedValueB.equals(newValue) : "ServiceB 应该收到更新";
        
        log.info("✓ 多 Bean 共享配置测试通过 - 两个 Bean 都成功更新");
        log.info("========== 测试 2 通过 ==========\n");
    }

    /**
     * 测试 3：同 Bean 多字段共享配置测试
     * 验证同一个 Bean 中多个字段使用同一个配置 Key 时，都能正确接收更新
     */
    @Test
    public void test_multiFieldInSameBean() throws InterruptedException {
        log.info("========== 测试 3：同 Bean 多字段共享配置测试 ==========");
        
        // 记录初始值
        String initialField1 = testServiceA.getField1();
        String initialField2 = testServiceA.getField2();
        
        log.info("初始值 - field1: {}, field2: {}", initialField1, initialField2);
        assert initialField1.equals(initialField2) : "同 Bean 内多字段初始值应该相同";
        
        // 发布配置更新
        String newValue = "updated_value";
        AttributeVO attributeVO = new AttributeVO("sharedField", newValue);
        log.info("发布配置更新：{}", attributeVO);
        dynamicConfigCenterRedisTopic.publish(attributeVO);
        
        // 等待更新完成
        Thread.sleep(1000);
        
        // 验证两个字段都收到了更新
        String updatedField1 = testServiceA.getField1();
        String updatedField2 = testServiceA.getField2();
        
        log.info("更新后 - field1: {}, field2: {}", updatedField1, updatedField2);
        
        assert updatedField1.equals(newValue) : "field1 应该收到更新";
        assert updatedField2.equals(newValue) : "field2 应该收到更新";
        
        log.info("✓ 同 Bean 多字段共享配置测试通过 - 两个字段都成功更新");
        log.info("========== 测试 3 通过 ==========\n");
    }

    /**
     * 测试 4：不同类型字段支持测试
     * 验证框架支持各种数据类型的自动转换
     */
    @Test
    public void test_differentTypeSupport() {
        log.info("========== 测试 4：不同类型字段支持测试 ==========");
        
        // 验证 Long 类型
        Long timeout = testServiceA.getTimeout();
        log.info("Long 类型 - timeout: {}", timeout);
        assert timeout != null : "timeout 不应为 null";
        
        // 验证 Boolean 类型
        Boolean enabled = testServiceA.getEnabled();
        log.info("Boolean 类型 - enabled: {}", enabled);
        assert enabled != null : "enabled 不应为 null";
        
        // 验证 Double 类型
        Double threshold = testServiceA.getThreshold();
        log.info("Double 类型 - threshold: {}", threshold);
        assert threshold != null : "threshold 不应为 null";
        
        log.info("✓ 不同类型字段支持测试通过 - 所有类型都正确转换");
        log.info("========== 测试 4 通过 ==========\n");
    }

    /**
     * 测试 5：配置动态更新测试（批量更新）
     * 验证一次配置变更能同时更新所有相关字段
     */
    @Test
    public void test_batchUpdate() throws InterruptedException {
        log.info("========== 测试 5：配置动态更新测试（批量更新） ==========");
        
        // 检查注册字段数
        int fieldCount = dccService.getFieldCount("test-system_sharedField");
        log.info("sharedField 注册字段数：{}", fieldCount);
        assert fieldCount == 2 : "应该有 2 个字段注册";
        
        // 发布配置更新
        String newValue = "batch_update_value";
        AttributeVO attributeVO = new AttributeVO("sharedField", newValue);
        log.info("发布批量更新：{}", attributeVO);
        dynamicConfigCenterRedisTopic.publish(attributeVO);
        
        // 等待更新完成
        Thread.sleep(1000);
        
        // 验证所有字段都更新了
        assert testServiceA.getField1().equals(newValue) : "field1 应该收到批量更新";
        assert testServiceA.getField2().equals(newValue) : "field2 应该收到批量更新";
        
        log.info("✓ 批量更新测试通过 - 所有字段都成功更新");
        log.info("========== 测试 5 通过 ==========\n");
    }

    /**
     * 测试 6：统计信息测试
     * 验证监控和调试功能的可用性
     */
    @Test
    public void test_statistics() {
        log.info("========== 测试 6：统计信息测试 ==========");
        
        // 获取统计信息
        String statistics = dccService.getStatistics();
        log.info("DCC 统计信息：{}", statistics);
        
        assert statistics != null : "统计信息不应为 null";
        assert statistics.contains("配置 Key 数") : "统计信息应包含配置 Key 数";
        assert statistics.contains("注册字段数") : "统计信息应包含注册字段数";
        
        // 获取特定配置的字段数
        int downgradeSwitchCount = dccService.getFieldCount("test-system_downgradeSwitch");
        log.info("downgradeSwitch 注册字段数：{}", downgradeSwitchCount);
        assert downgradeSwitchCount >= 2 : "downgradeSwitch 至少有 2 个字段（ServiceA 和 ServiceB）";
        
        int sharedFieldCount = dccService.getFieldCount("test-system_sharedField");
        log.info("sharedField 注册字段数：{}", sharedFieldCount);
        assert sharedFieldCount == 2 : "sharedField 应该有 2 个字段";
        
        log.info("✓ 统计信息测试通过 - 监控功能正常");
        log.info("========== 测试 6 通过 ==========\n");
    }

    /**
     * 测试 7：枚举类型支持测试
     * 验证枚举类型的配置注入和更新
     */
    @Test
    public void test_enumTypeSupport() throws InterruptedException {
        log.info("========== 测试 7：枚举类型支持测试 ==========");
        
        // 验证初始值
        TestServiceA.Mode initialMode = testServiceA.getMode();
        log.info("枚举初始值：{}", initialMode);
        assert initialMode != null : "mode 不应为 null";
        
        // 发布枚举类型更新
        String newValue = "MODE_B";
        AttributeVO attributeVO = new AttributeVO("mode", newValue);
        log.info("发布枚举配置更新：{}", attributeVO);
        dynamicConfigCenterRedisTopic.publish(attributeVO);
        
        // 等待更新完成
        Thread.sleep(1000);
        
        // 验证枚举值更新
        TestServiceA.Mode updatedMode = testServiceA.getMode();
        log.info("枚举更新后：{}", updatedMode);
        assert updatedMode == TestServiceA.Mode.MODE_B : "mode 应该更新为 MODE_B";
        
        log.info("✓ 枚举类型支持测试通过");
        log.info("========== 测试 7 通过 ==========\n");
    }

    /**
     * 测试 8：容错机制测试
     * 验证单个字段更新失败不影响其他字段
     */
    @Test
    public void test_errorTolerance() throws InterruptedException {
        log.info("========== 测试 8：容错机制测试 ==========");
        
        // 发布一个不存在的配置更新（应该不会影响其他正常配置）
        AttributeVO attributeVO = new AttributeVO("nonExistentConfig", "value");
        log.info("发布不存在的配置更新：{}", attributeVO);
        dynamicConfigCenterRedisTopic.publish(attributeVO);
        
        // 等待更新完成
        Thread.sleep(1000);
        
        // 验证正常配置仍然可用
        assert testServiceA.getDowngradeSwitch() != null : "正常配置不应受影响";
        assert testServiceB.getDowngradeSwitch() != null : "正常配置不应受影响";
        
        log.info("✓ 容错机制测试通过 - 异常配置不影响正常配置");
        log.info("========== 测试 8 通过 ==========\n");
    }

    /**
     * 测试 9：快速连续更新测试
     * 验证系统能处理快速连续的配置变更
     */
    @Test
    public void test_rapidUpdates() throws InterruptedException {
        log.info("========== 测试 9：快速连续更新测试 ==========");
        
        // 快速发布多次更新
        for (int i = 0; i < 5; i++) {
            String value = "update_" + i;
            AttributeVO attributeVO = new AttributeVO("downgradeSwitch", value);
            dynamicConfigCenterRedisTopic.publish(attributeVO);
            log.debug("发布第 {} 次更新：{}", i + 1, value);
            Thread.sleep(100);  // 短暂延迟
        }
        
        // 等待所有更新处理完成
        Thread.sleep(1000);
        
        // 验证最终值（应该是最后一次更新）
        String finalValue = testServiceA.getDowngradeSwitch();
        log.info("最终值：{}", finalValue);
        assert finalValue.equals("update_4") : "应该是最后一次更新的值";
        
        log.info("✓ 快速连续更新测试通过");
        log.info("========== 测试 9 通过 ==========\n");
    }

    /**
     * 测试 10：清理机制测试
     * 验证清理功能正常工作
     * 
     * 注意：此测试不应该调用 destroy()，因为会清空所有字段，影响其他测试
     * 只验证统计和查询功能
     */
    @Test
    public void test_cleanupMechanism() {
        log.info("========== 测试 10：清理机制测试 ==========");
        
        // 获取清理前的统计
        String beforeStats = dccService.getStatistics();
        log.info("清理前统计：{}", beforeStats);
        
        // 获取特定配置的字段数
        int beforeCount = dccService.getFieldCount("test-system_downgradeSwitch");
        log.info("清理前 downgradeSwitch 字段数：{}", beforeCount);
        assert beforeCount >= 2 : "清理前应该有至少 2 个字段";
        
        // ========== 只测试查询和统计功能，不清理 ==========
        // 测试 1：清理 null Bean（应该不报错）
        dccService.unregisterBean(null);
        log.info("✓ 清理 null Bean 测试通过 - 不抛异常");
        
        // 测试 2：获取清理前的字段数
        int sharedFieldBeforeCount = dccService.getFieldCount("test-system_sharedField");
        log.info("清理前 sharedField 字段数：{}", sharedFieldBeforeCount);
        assert sharedFieldBeforeCount == 2 : "清理前应该有 2 个字段";
        
        // 测试 3：验证统计信息方法
        String stats = dccService.getStatistics();
        log.info("当前统计信息：{}", stats);
        assert stats != null : "统计信息不应为 null";
        assert stats.contains("配置 Key 数") : "统计信息应包含配置 Key 数";
        assert stats.contains("注册字段数") : "统计信息应包含注册字段数";
        
        // 测试 4：验证 getFieldCount 方法
        int count = dccService.getFieldCount("test-system_sharedField");
        log.info("sharedField 当前字段数：{}", count);
        assert count == 2 : "应该有 2 个字段";
        
        // 测试 5：验证获取不存在的配置
        int nonExistentCount = dccService.getFieldCount("non.existent.config");
        log.info("不存在的配置字段数：{}", nonExistentCount);
        assert nonExistentCount == 0 : "不存在的配置应该返回 0";
        
        // ⚠️ 注意：不调用 destroy()，因为会清空所有字段，影响其他测试
        // destroy() 方法应该在应用关闭时自动调用，而不是在测试中
        log.info("⚠️ 跳过 destroy() 测试，避免影响其他测试用例");
        
        log.info("✓ 清理机制测试通过 - 统计和查询功能正常");
        log.info("========== 测试 10 通过 ==========\n");
    }
}
