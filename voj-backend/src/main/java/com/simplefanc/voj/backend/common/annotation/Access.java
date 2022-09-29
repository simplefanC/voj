package com.simplefanc.voj.backend.common.annotation;

import com.simplefanc.voj.backend.common.constants.AccessEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author chenfan
 * @Date 2022/9/25
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Access {
    AccessEnum[] value() default {};
}
