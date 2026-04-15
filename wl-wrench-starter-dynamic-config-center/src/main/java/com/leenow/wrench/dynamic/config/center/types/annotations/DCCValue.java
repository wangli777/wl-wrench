package com.leenow.wrench.dynamic.config.center.types.annotations;

import java.lang.annotation.*;

/**
 * @author: WangLi
 * @date: 2026/4/15 19:23
 * @description: 动态配置中心注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface DCCValue {
    String value() default "";
}
