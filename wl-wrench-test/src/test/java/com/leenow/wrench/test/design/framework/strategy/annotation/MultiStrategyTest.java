package com.leenow.wrench.test.design.framework.strategy.annotation;

import com.leenow.wrench.design.framework.strategy.DynamicContext;
import com.leenow.wrench.design.framework.strategy.StrategyHandler;
import com.leenow.wrench.design.framework.strategy.base.BaseResponse;
import com.leenow.wrench.test.design.framework.strategy.annotation.manager.StrategyRegistry;
import com.leenow.wrench.test.design.framework.strategy.annotation.strategy.FreeShippingStrategy;
import com.leenow.wrench.test.design.framework.strategy.annotation.strategy.FreeShippingStrategy.ShippingResponse;
import com.leenow.wrench.test.design.framework.strategy.annotation.strategy.FullReductionStrategy;
import com.leenow.wrench.test.design.framework.strategy.annotation.strategy.FullReductionStrategy.ReductionResponse;
import com.leenow.wrench.test.design.framework.strategy.annotation.strategy.OrderRequest;
import com.leenow.wrench.test.design.framework.strategy.annotation.strategy.VIPDiscountStrategy;
import com.leenow.wrench.test.design.framework.strategy.annotation.strategy.VIPDiscountStrategy.VIPResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 多策略使用示例测试（Spring Boot 版本）
 * 
 * <p>演示如何使用 Spring Boot 集成测试管理多个策略。</p>
 * 
 * <h3>测试场景：</h3>
 * <ul>
 *     <li>测试 1：VIP 折扣策略 - VIP 会员（等级 2）下单 500 元</li>
 *     <li>测试 2：满减策略 - 普通用户下单 600 元</li>
 *     <li>测试 3：多重优惠叠加 - 高级 VIP 会员（等级 3）下单 1200 元</li>
 *     <li>测试 4：不匹配任何策略 - 普通用户下单 50 元</li>
 *     <li>测试 5：只执行第一个匹配的策略 - 使用 executeFirstMatching 方法</li>
 *     <li>测试 6：策略优先级验证 - 验证策略按优先级顺序执行</li>
 * </ul>
 * 
 * @author WangLi
 * @date 2026/4/16
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiStrategyTest {
    
    @Resource
    private StrategyRegistry registry;
    
    @Resource
    private VIPDiscountStrategy vipDiscountStrategy;
    
    @Resource
    private FreeShippingStrategy freeShippingStrategy;
    
    @Resource
    private FullReductionStrategy fullReductionStrategy;
    
    @Before
    public void setUp() {
        // 清空并重新注册策略
        registry.clear();
        
        // 注册所有策略（通过 Spring 注入）
        registry.register(vipDiscountStrategy);
        registry.register(freeShippingStrategy);
        registry.register(fullReductionStrategy);
        
        log.info("策略注册表初始化完成");
    }
    
    @After
    public void tearDown() {
        if (registry != null) {
            registry.clear();
        }
    }
    
    /**
     * 测试 1：VIP 折扣策略
     * 
     * <p>场景：VIP 会员（等级 2）下单 500 元，应该匹配 VIP 折扣策略和免运费策略。</p>
     */
    @Test
    public void testVIPDiscountStrategy() throws Exception {
        log.info("========== 测试 1: VIP 折扣策略 ==========");
        
        // 创建请求：VIP 会员（等级 2），订单金额 500 元
        OrderRequest request = new OrderRequest(500.0, 2, "user001");
        
        // 创建上下文并设置数据
        DynamicContext context = new DynamicContext();
        context.setValue("userName", "张三", String.class);
        context.setValue("memberLevel", 2, Integer.class);
        
        try {
            // 查找匹配的策略
            List<StrategyHandler> matchingStrategies = 
                registry.findMatchingStrategies(request, context);
            
            // 验证匹配的策略数量（VIP 折扣 + 免运费 + 满减，因为 500 元>=300 元）
            assertEquals("应该匹配 3 个策略", 3, matchingStrategies.size());
            
            // 执行所有匹配的策略
            List<? extends BaseResponse> results = registry.executeAllMatching(request, context);
            
            // 验证结果
            assertEquals("应该有 3 个执行结果", 3, results.size());
            
            // 验证 VIP 折扣结果（按优先级排序，第一个是 VIP 折扣）
            VIPResponse vipResponse = (VIPResponse) results.get(0);
            assertEquals("VIP 用户应该是 7 折", 0.7, vipResponse.getDiscount(), 0.01);
            assertEquals("节省金额应该是 150 元", 150.0, vipResponse.getSavedAmount(), 0.01);
            assertEquals("会员等级应该是 2", 2, vipResponse.getMemberLevel());
            log.info("VIP 折扣结果：{}", vipResponse.getMessage());
            
            // 验证免运费结果（第二个是免运费）
            ShippingResponse shippingResponse = (ShippingResponse) results.get(1);
            assertTrue("应该免运费", shippingResponse.isFreeShipping());
            assertEquals("原始运费应该是 10 元", 10.0, shippingResponse.getOriginalFee(), 0.01);
            assertEquals("最终运费应该是 0 元", 0.0, shippingResponse.getFinalFee(), 0.01);
            log.info("免运费结果：{}", shippingResponse.getMessage());
            
            // 验证满减结果（第三个是满减）
            ReductionResponse reductionResponse = (ReductionResponse) results.get(2);
            assertEquals("满减金额应该是 80 元（满 500 减 80）", 80.0, reductionResponse.getReductionAmount(), 0.01);
            assertEquals("最终支付应该是 420 元", 420.0, reductionResponse.getFinalAmount(), 0.01);
            log.info("满减结果：{}", reductionResponse.getMessage());
            
        } finally {
            context.close();
        }
        
        log.info("========== 测试 1 完成 ==========\n");
    }
    
    /**
     * 测试 2：满减策略
     * 
     * <p>场景：普通用户下单 600 元，应该匹配满减策略。</p>
     */
    @Test
    public void testFullReductionStrategy() throws Exception {
        log.info("========== 测试 2: 满减策略 ==========");
        
        // 创建请求：普通用户（等级 0），订单金额 600 元
        OrderRequest request = new OrderRequest(600.0, 0, "user002");
        
        // 创建上下文并设置数据
        DynamicContext context = new DynamicContext();
        context.setValue("userName", "李四", String.class);
        context.setValue("memberLevel", 0, Integer.class);
        
        try {
            // 查找匹配的策略
            List<StrategyHandler> matchingStrategies = 
                registry.findMatchingStrategies(request, context);
            
            // 验证匹配的策略数量（免运费 + 满减，因为金额>=200 且>=300）
            assertEquals("应该匹配 2 个策略", 2, matchingStrategies.size());
            
            // 执行所有匹配的策略
            List<? extends BaseResponse> results = registry.executeAllMatching(request, context);
            
            // 验证结果
            assertEquals("应该有 2 个执行结果", 2, results.size());
            
            // 验证免运费结果（按优先级排序，第一个是免运费）
            ShippingResponse shippingResponse = (ShippingResponse) results.get(0);
            assertTrue("应该免运费（金额>=200）", shippingResponse.isFreeShipping());
            assertEquals("原始运费应该是 10 元", 10.0, shippingResponse.getOriginalFee(), 0.01);
            assertEquals("最终运费应该是 0 元", 0.0, shippingResponse.getFinalFee(), 0.01);
            log.info("免运费结果：{}", shippingResponse.getMessage());
            
            // 验证满减结果（第二个是满减）
            ReductionResponse reductionResponse = (ReductionResponse) results.get(1);
            assertEquals("满减金额应该是 80 元（满 500 减 80）", 80.0, reductionResponse.getReductionAmount(), 0.01);
            assertEquals("最终支付应该是 520 元", 520.0, reductionResponse.getFinalAmount(), 0.01);
            log.info("满减结果：{}", reductionResponse.getMessage());
            
        } finally {
            context.close();
        }
        
        log.info("========== 测试 2 完成 ==========\n");
    }
    
    /**
     * 测试 3：多重优惠叠加
     * 
     * <p>场景：高级 VIP 会员（等级 3）下单 1200 元，应该匹配所有 3 个策略。</p>
     */
    @Test
    public void testMultipleStrategiesStacking() throws Exception {
        log.info("========== 测试 3: 多重优惠叠加 ==========");
        
        // 创建请求：高级 VIP 会员（等级 3），订单金额 1200 元
        OrderRequest request = new OrderRequest(1200.0, 3, "user003");
        
        // 创建上下文并设置数据
        DynamicContext context = new DynamicContext();
        context.setValue("userName", "王五", String.class);
        context.setValue("memberLevel", 3, Integer.class);
        
        try {
            // 查找匹配的策略
            List<StrategyHandler> matchingStrategies = 
                registry.findMatchingStrategies(request, context);
            
            // 验证匹配的策略数量（VIP 折扣 + 免运费 + 满减）
            assertEquals("应该匹配 3 个策略", 3, matchingStrategies.size());
            
            // 执行所有匹配的策略
            List<? extends BaseResponse> results = registry.executeAllMatching(request, context);
            
            // 验证结果
            assertEquals("应该有 3 个执行结果", 3, results.size());
            
            // 验证 VIP 折扣结果（按优先级排序，第一个是 VIP 折扣）
            VIPResponse vipResponse = (VIPResponse) results.get(0);
            assertEquals("高级 VIP 用户应该是 6 折", 0.6, vipResponse.getDiscount(), 0.01);
            assertEquals("节省金额应该是 480 元", 480.0, vipResponse.getSavedAmount(), 0.01);
            assertEquals("会员等级应该是 3", 3, vipResponse.getMemberLevel());
            log.info("VIP 折扣结果：{}", vipResponse.getMessage());
            
            // 验证免运费结果（第二个是免运费）
            ShippingResponse shippingResponse = (ShippingResponse) results.get(1);
            assertTrue("应该免运费", shippingResponse.isFreeShipping());
            log.info("免运费结果：{}", shippingResponse.getMessage());
            
            // 验证满减结果（第三个是满减）
            ReductionResponse reductionResponse = (ReductionResponse) results.get(2);
            assertEquals("满减金额应该是 200 元（满 1000 减 200）", 200.0, reductionResponse.getReductionAmount(), 0.01);
            assertEquals("最终支付应该是 1000 元", 1000.0, reductionResponse.getFinalAmount(), 0.01);
            log.info("满减结果：{}", reductionResponse.getMessage());
            
        } finally {
            context.close();
        }
        
        log.info("========== 测试 3 完成 ==========\n");
    }
    
    /**
     * 测试 4：不匹配任何策略
     * 
     * <p>场景：普通用户下单 50 元，不满足任何优惠条件。</p>
     */
    @Test
    public void testNoMatchingStrategy() throws Exception {
        log.info("========== 测试 4: 不匹配任何策略 ==========");
        
        // 创建请求：普通用户（等级 0），订单金额 50 元
        OrderRequest request = new OrderRequest(50.0, 0, "user004");
        
        // 创建上下文并设置数据
        DynamicContext context = new DynamicContext();
        context.setValue("userName", "赵六", String.class);
        context.setValue("memberLevel", 0, Integer.class);
        
        try {
            // 查找匹配的策略
            List<StrategyHandler> matchingStrategies = 
                registry.findMatchingStrategies(request, context);
            
            // 验证没有匹配的策略
            assertEquals("不应该匹配任何策略", 0, matchingStrategies.size());
            
            // 执行所有匹配的策略
            List<? extends BaseResponse> results = registry.executeAllMatching(request, context);
            assertTrue("执行结果应该为空", results.isEmpty());
            
            log.info("该用户不满足任何优惠条件");
            
        } finally {
            context.close();
        }
        
        log.info("========== 测试 4 完成 ==========\n");
    }
    
    /**
     * 测试 5：只执行第一个匹配的策略
     * 
     * <p>场景：使用 executeFirstMatching 方法只执行优先级最高的策略。</p>
     */
    @Test
    public void testExecuteFirstMatching() throws Exception {
        log.info("========== 测试 5: 只执行第一个匹配的策略 ==========");
        
        // 创建请求：VIP 会员（等级 1），订单金额 400 元
        OrderRequest request = new OrderRequest(400.0, 1, "user005");
        
        // 创建上下文并设置数据
        DynamicContext context = new DynamicContext();
        context.setValue("userName", "钱七", String.class);
        context.setValue("memberLevel", 1, Integer.class);
        
        try {
            // 执行第一个匹配的策略（优先级最高的）
            Object result = registry.executeFirstMatching(request, context);
            
            // 验证结果（应该只返回 VIP 折扣策略的结果）
            assertNotNull("应该有执行结果", result);
            assertTrue("结果应该是 VIPResponse 类型", result instanceof VIPResponse);
            
            VIPResponse vipResponse = (VIPResponse) result;
            assertEquals("VIP 用户应该是 85 折", 0.85, vipResponse.getDiscount(), 0.01);
            assertEquals("节省金额应该是 60 元", 60.0, vipResponse.getSavedAmount(), 0.01);
            log.info("第一个匹配策略的结果：{}", vipResponse.getMessage());
            
        } finally {
            context.close();
        }
        
        log.info("========== 测试 5 完成 ==========\n");
    }
    
    /**
     * 测试 6：策略优先级验证
     * 
     * <p>验证策略按优先级顺序执行（priority 值越小优先级越高）。</p>
     */
    @Test
    public void testStrategyPriority() throws Exception {
        log.info("========== 测试 6: 策略优先级验证 ==========");
        
        // 创建请求：满足所有策略条件
        OrderRequest request = new OrderRequest(1200.0, 3, "user006");
        
        // 创建上下文
        DynamicContext context = new DynamicContext();
        context.setValue("userName", "测试用户", String.class);
        context.setValue("memberLevel", 3, Integer.class);
        
        try {
            // 获取所有策略
            List<StrategyHandler> allStrategies = registry.getAllStrategies();
            
            log.info("注册的策略顺序（按优先级）：");
            for (int i = 0; i < allStrategies.size(); i++) {
                StrategyHandler strategy = allStrategies.get(i);
                String strategyName = strategy.getClass().getSimpleName();
                log.info("  {}. {}", (i + 1), strategyName);
            }
            
            // 验证策略数量
            assertEquals("应该有 3 个策略", 3, allStrategies.size());
            
            // 验证策略顺序（按优先级排序）
            // VIPDiscountStrategy (priority=1) 应该排第一
            // FreeShippingStrategy (priority=2) 应该排第二
            // FullReductionStrategy (priority=3) 应该排第三
            assertTrue("第一个策略应该是 VIPDiscountStrategy", 
                      allStrategies.get(0) instanceof VIPDiscountStrategy);
            assertTrue("第二个策略应该是 FreeShippingStrategy", 
                      allStrategies.get(1) instanceof FreeShippingStrategy);
            assertTrue("第三个策略应该是 FullReductionStrategy", 
                      allStrategies.get(2) instanceof FullReductionStrategy);
            
            log.info("策略优先级验证通过");
            
        } finally {
            context.close();
        }
        
        log.info("========== 测试 6 完成 ==========\n");
    }
}
