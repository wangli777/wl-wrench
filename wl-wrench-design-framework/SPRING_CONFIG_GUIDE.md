# Spring 环境配置指南

本文档介绍如何在 Spring 环境中手动配置和使用 `wl-wrench-design-framework` 的策略模式功能。

## 📋 目录

- [背景说明](#背景说明)
- [方式 1：Java Config 配置](#方式-1java-config-配置推荐)
- [方式 2：Spring Boot 配置](#方式-2spring-boot-配置最常用)
- [方式 3：使用@Component 自动初始化](#方式-3 使用 component-自动初始化)
- [四种方式对比](#四种方式对比)
- [最佳实践](#最佳实践推荐)

---

## 背景说明

`wl-wrench-design-framework` 采用纯 Java 实现，不依赖任何框架。在 Spring 环境中使用时，需要手动配置 `StrategyRegistry` Bean。

**核心类**：
- `StrategyRegistry`：策略注册表，管理所有策略的注册和执行
- `StrategyHandler`：策略处理器接口
- `BaseRequest`：请求参数基类
- `BaseResponse`：响应参数基类
- `DynamicContext`：动态上下文

---

## 方式 1：Java Config 配置（推荐）

适用于传统 Spring 项目。

### 配置类

```java
@Configuration
public class StrategyConfig {
    
    /**
     * 配置 StrategyRegistry Bean
     */
    @Bean
    public StrategyRegistry strategyRegistry(
            VipDiscountStrategy vipDiscountStrategy,
            FullReductionStrategy fullReductionStrategy,
            FreeShippingStrategy freeShippingStrategy) {
        
        StrategyRegistry registry = new StrategyRegistry();
        registry.register(vipDiscountStrategy);
        registry.register(fullReductionStrategy);
        registry.register(freeShippingStrategy);
        
        return registry;
    }
    
    /**
     * 配置策略 Bean
     */
    @Bean
    public VipDiscountStrategy vipDiscountStrategy() {
        return new VipDiscountStrategy();
    }
    
    @Bean
    public FullReductionStrategy fullReductionStrategy() {
        return new FullReductionStrategy();
    }
    
    @Bean
    public FreeShippingStrategy freeShippingStrategy() {
        return new FreeShippingStrategy();
    }
}
```

### 使用方式

```java
ApplicationContext context = new AnnotationConfigApplicationContext(StrategyConfig.class);
StrategyRegistry registry = (StrategyRegistry) context.getBean("strategyRegistry");

OrderRequest request = OrderRequest.builder()
    .userId("user123")
    .amount(500.0)
    .userLevel(2)
    .build();

DynamicContext context = new DynamicContext();
OrderResponse response = registry.executeFirstMatching(request, context);
```

### 优点
- ✅ 类型安全
- ✅ 配置灵活
- ✅ 易于重构

### 缺点
- ⚠️ 需要手动注册每个策略

---

## 方式 2：Spring Boot 配置（最常用）

适用于 Spring Boot 项目，推荐使用。

### 主应用类配置

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
    
    /**
     * 配置 StrategyRegistry Bean
     * 自动注册所有 StrategyHandler 实现类
     */
    @Bean
    public StrategyRegistry strategyRegistry(List<StrategyHandler<?, ?, ?>> strategyHandlers) {
        StrategyRegistry registry = new StrategyRegistry();
        
        // 自动注册所有注入的策略处理器
        for (StrategyHandler<?, ?, ?> handler : strategyHandlers) {
            registry.register(handler);
        }
        
        return registry;
    }
    
    /**
     * 配置策略 Bean
     * 这些 Bean 会被自动注入到 strategyRegistry 方法中
     */
    @Bean
    public VipDiscountStrategy vipDiscountStrategy() {
        return new VipDiscountStrategy();
    }
    
    @Bean
    public FullReductionStrategy fullReductionStrategy() {
        return new FullReductionStrategy();
    }
    
    @Bean
    public FreeShippingStrategy freeShippingStrategy() {
        return new FreeShippingStrategy();
    }
}
```

### 业务代码

```java
@Service
public class OrderService {
    
    @Autowired
    private StrategyRegistry registry;  // 自动注入
    
    public OrderResponse processOrder(OrderRequest request) {
        DynamicContext context = new DynamicContext();
        
        // 执行第一个匹配的策略
        return registry.executeFirstMatching(request, context);
        
        // 或者执行所有匹配的策略
        // List<OrderResponse> responses = registry.executeAllMatching(request, context);
    }
}
```

### 简化版：使用 Java 8 Stream

```java
@Bean
public StrategyRegistry strategyRegistry(List<StrategyHandler<?, ?, ?>> strategyHandlers) {
    StrategyRegistry registry = new StrategyRegistry();
    strategyHandlers.forEach(registry::register);
    return registry;
}
```

### 优点
- ✅ 代码简洁
- ✅ 自动注册所有策略
- ✅ 与 Spring Boot 完美集成
- ✅ 易于扩展

### 缺点
- ⚠️ 需要 Spring Boot 环境

---

## 方式 3：使用@Component 自动初始化

适用于需要高度自动化的场景。

### 方式 3.1：使用 FactoryBean

```java
@Component
public class StrategyRegistryFactory implements InitializingBean {
    
    @Autowired
    private List<StrategyHandler<?, ?, ?>> strategyHandlers;
    
    private StrategyRegistry registry;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        registry = new StrategyRegistry();
        for (StrategyHandler<?, ?, ?> handler : strategyHandlers) {
            registry.register(handler);
        }
    }
    
    @Bean
    public StrategyRegistry getStrategyRegistry() {
        return registry;
    }
}
```

### 方式 3.2：使用@ConfigurationProperties（不推荐）

```java
@Component
public class StrategyRegistryAutoConfig {
    
    @Autowired
    private List<StrategyHandler<?, ?, ?>> strategyHandlers;
    
    @Bean
    @Primary
    public StrategyRegistry strategyRegistry() {
        StrategyRegistry registry = new StrategyRegistry();
        strategyHandlers.forEach(registry::register);
        return registry;
    }
}
```

### 方式 3.3：使用@PostConstruct

```java
@Component
public class StrategyRegistryInitializer {
    
    @Autowired
    private List<StrategyHandler<?, ?, ?>> strategyHandlers;
    
    private StrategyRegistry registry;
    
    @PostConstruct
    public void init() {
        registry = new StrategyRegistry();
        strategyHandlers.forEach(registry::register);
    }
    
    public StrategyRegistry getRegistry() {
        return registry;
    }
}
```

### 优点
- ✅ 自动化程度高
- ✅ 减少配置代码

### 缺点
- ⚠️ 实现稍复杂
- ⚠️ 调试相对困难

---

## 四种方式对比

| 配置方式 | 优点 | 缺点 | 适用场景 | 推荐指数 |
|---------|------|------|----------|---------|
| **XML 配置** | 集中管理，清晰直观 | 配置繁琐，不灵活，类型不安全 | 老项目维护 | ⭐⭐ |
| **Java Config** | 类型安全，灵活，易重构 | 需要手动注册每个策略 | 传统 Spring 项目 | ⭐⭐⭐⭐ |
| **Spring Boot** | 代码简洁，自动注册，易扩展 | 需要 Spring Boot 环境 | 新项目（推荐） | ⭐⭐⭐⭐⭐ |
| **@Component** | 自动化程度高 | 实现复杂，调试困难 | 特殊需求场景 | ⭐⭐⭐ |

---

## 最佳实践推荐

### 对于 Spring Boot 项目

**推荐使用方式 2（Spring Boot 配置）**，具体实现如下：

#### 1. 主应用类

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
    
    // 配置 StrategyRegistry - 核心配置
    @Bean
    public StrategyRegistry strategyRegistry(List<StrategyHandler<?, ?, ?>> strategyHandlers) {
        StrategyRegistry registry = new StrategyRegistry();
        strategyHandlers.forEach(registry::register);
        return registry;
    }
}
```

#### 2. 策略实现类

```java
/**
 * VIP 折扣策略
 */
@Component  // 或者使用 @Bean 在主应用类中定义
@Strategy(name = "vipDiscount", priority = 1)
public class VipDiscountStrategy implements StrategyHandler<OrderRequest, OrderContext, OrderResponse> {
    
    @Override
    public boolean match(OrderRequest request, OrderContext context) {
        return request.getUserLevel() != null && request.getUserLevel() >= 1;
    }
    
    @Override
    public OrderResponse apply(OrderRequest request, OrderContext context) {
        double discount = calculateDiscount(request.getUserLevel());
        double finalAmount = request.getAmount() * discount;
        
        return OrderResponse.builder()
                .orderId("VIP-" + System.currentTimeMillis())
                .finalAmount(finalAmount)
                .discount(discount)
                .message(String.format("VIP%d 会员享受%.2f 折优惠", request.getUserLevel(), discount))
                .build();
    }
    
    private double calculateDiscount(Integer userLevel) {
        switch (userLevel) {
            case 3: return 0.85;  // VIP3: 85 折
            case 2: return 0.90;  // VIP2: 9 折
            case 1: return 0.95;  // VIP1: 95 折
            default: return 1.0;   // 无折扣
        }
    }
}
```

#### 3. 业务服务类

```java
@Service
public class OrderService {
    
    @Autowired
    private StrategyRegistry registry;
    
    public OrderResponse processOrder(OrderRequest request) {
        DynamicContext context = new DynamicContext();
        
        // 设置上下文数据（可选）
        context.setValue("userId", request.getUserId(), String.class);
        context.setValue("processTime", System.currentTimeMillis(), Long.class);
        
        // 执行策略
        return registry.executeFirstMatching(request, context);
    }
    
    public List<OrderResponse> processAllStrategies(OrderRequest request) {
        DynamicContext context = new DynamicContext();
        return registry.executeAllMatching(request, context);
    }
}
```

#### 4. 单元测试

```java
@SpringBootTest
public class OrderServiceTest {
    
    @Autowired
    private OrderService orderService;
    
    @Test
    public void testVipDiscount() {
        OrderRequest request = OrderRequest.builder()
            .userId("user123")
            .amount(500.0)
            .userLevel(2)
            .build();
        
        OrderResponse response = orderService.processOrder(request);
        
        assertNotNull(response);
        assertEquals(450.0, response.getFinalAmount(), 0.01); // 9 折
        assertTrue(response.getMessage().contains("VIP"));
    }
}
```

---

## 完整示例项目结构

```
my-project/
├── src/main/java/com/example/
│   ├── MyApplication.java                    # Spring Boot 主应用类
│   ├── config/
│   │   └── StrategyConfig.java               # 策略配置（可选）
│   ├── strategy/
│   │   ├── VipDiscountStrategy.java          # VIP 折扣策略
│   │   ├── FullReductionStrategy.java        # 满减策略
│   │   └── FreeShippingStrategy.java         # 免运费策略
│   ├── model/
│   │   ├── OrderRequest.java                 # 请求模型
│   │   ├── OrderResponse.java                # 响应模型
│   │   └── OrderContext.java                 # 上下文模型
│   └── service/
│       └── OrderService.java                 # 业务服务
├── src/test/java/com/example/
│   └── OrderServiceTest.java                 # 单元测试
└── pom.xml
```

---

## 常见问题

### Q1: 策略没有被自动注册怎么办？

**A**: 确保策略类被 Spring 扫描到：
- 使用 `@Component` 注解
- 或者在主应用类中使用 `@Bean` 显式定义
- 确保包路径在 Spring 扫描范围内

### Q2: 如何控制策略的执行顺序？

**A**: 使用 `@Strategy` 注解的 `priority` 属性：
```java
@Strategy(name = "vipDiscount", priority = 1)  // 优先级最高
public class VipDiscountStrategy { ... }

@Strategy(name = "fullReduction", priority = 2)  // 优先级次之
public class FullReductionStrategy { ... }
```

### Q3: 如何在运行时动态添加策略？

**A**: 直接调用 `StrategyRegistry.register()` 方法：
```java
@Autowired
private StrategyRegistry registry;

public void addStrategy(StrategyHandler<?, ?, ?> strategy) {
    registry.register(strategy);
}
```

### Q4: 纯 Java 项目如何使用？

**A**: 直接手动创建和注册：
```java
public class Main {
    public static void main(String[] args) {
        StrategyRegistry registry = new StrategyRegistry();
        registry.register(new VipDiscountStrategy());
        registry.register(new FullReductionStrategy());
        
        OrderRequest request = ...;
        OrderResponse response = registry.executeFirstMatching(request, new DynamicContext());
    }
}
```

---

## 相关文档

- [策略模式设计文档](./STRATEGY_PATTERN_COMPARISON.md)
- [泛型设计优化](./GENERIC_DESIGN_OPTIMIZATION.md)
- [AbstractStrategyRegistry API 文档](../src/main/java/com/leenow/wrench/design/framework/strategy/manager/AbstractStrategyRegistry.java)

---

**最后更新时间**：2026-04-16  
**文档版本**：1.0.0
