package com.simplefanc.voj.utils;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 公用工具类
 * @author Administrator
 *
 */
public class Tools {
    public static <T> List<Class<? extends T>> findSubClasses(String packagePath, Class<T> parentClass) throws ClassNotFoundException {
        List<Class<? extends T>> result = new ArrayList<Class<? extends T>>();
        
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(parentClass));

        Set<BeanDefinition> components = provider.findCandidateComponents(packagePath);
        for (BeanDefinition component : components) {
            Class<? extends T> clazz = (Class<? extends T>) Class.forName(component.getBeanClassName());
            result.add(clazz);
        }
        return result;
    }
}
