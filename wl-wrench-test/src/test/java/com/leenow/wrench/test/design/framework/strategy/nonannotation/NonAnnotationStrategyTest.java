package com.leenow.wrench.test.design.framework.strategy.nonannotation;

import com.leenow.wrench.test.design.framework.strategy.nonannotation.node.RootNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * 非注解方式策略模式单元测试
 *
 * @author WangLi
 * @date 2026/4/16
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class NonAnnotationStrategyTest {
    @Resource
    private RootNode rootNode;

    @Test
    public void test() throws Exception {
        OrderResponse result = rootNode.apply(new OrderRequest("2", 100.0, 1, "normal"));

        log.info("测试结果:{}", result);
    }
}
