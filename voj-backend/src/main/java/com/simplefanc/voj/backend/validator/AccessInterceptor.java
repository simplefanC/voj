package com.simplefanc.voj.backend.validator;

import com.simplefanc.voj.backend.common.annotation.Access;
import com.simplefanc.voj.backend.common.constants.AccessEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @Author chenfan
 * @Date 2022/5/9
 */
@Component
@RequiredArgsConstructor
public class AccessInterceptor implements HandlerInterceptor {

    private final AccessValidator accessValidator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = HandlerMethod.class.cast(handler);
            Access access = this.getAnnotation(handlerMethod.getMethod(), handlerMethod.getBeanType(), Access.class);
            if (access == null || access.value().length == 0) {
                return true;
            }
            for (AccessEnum value : access.value()) {
                accessValidator.validateAccess(value);
            }
            return true;
        } else if (handler instanceof ResourceHttpRequestHandler) {
            // 静态资源的请求不处理
            return true;
        }
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    /**
     * 先从method上获取注解，获取不到再从class上获取
     *
     * @param method
     * @param clazz
     * @param annotationClass
     * @param <T>
     * @return 注解对象
     */
    public <T extends Annotation> T getAnnotation(Method method, Class<?> clazz, Class<T> annotationClass) {
        T annotation = AnnotationUtils.getAnnotation(method, annotationClass);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(clazz, annotationClass);
        }
        return annotation;
    }
}
