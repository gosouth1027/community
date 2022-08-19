package com.sadness.community.aspect;

import org.springframework.web.bind.annotation.RequestAttribute;

import java.lang.annotation.Annotation;

/**
 * @version 1.0
 * @Date 2022/6/25 9:51
 * @Author SadAndBeautiful
 */
public class NonWebRequestAttributes implements RequestAttribute {
    @Override
    public String value() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean required() {
        return false;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}
