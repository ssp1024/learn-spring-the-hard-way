package com.ssp1024.tutorial.scanbeans.core;


import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationBeanFactory {
    private Map<String, Object> beansMap = new HashMap<>();
    private Map<Class, String[]> beansNameByTypeCache = new HashMap<>();

    public static AnnotationBeanFactory fromBasePackage(String basePackage) {
        AnnotationBeanFactory instance = new AnnotationBeanFactory();
        instance.init(basePackage);
        return instance;
    }

    public Object getBean(String name) {
        return beansMap.get(name);
    }

    @SneakyThrows
    public <T> T getBean(Class<T> type) {
        String[] names = beansNameByTypeCache.get(type);
        if (names == null) {
            names = doGetBeansNameByType(type);
            beansNameByTypeCache.put(type, names);
        }

        if (names.length < 1) {
            throw new Exception("no bean defined for type: " + type);
        }
        if (names.length > 1) {
            throw new Exception("multiple bean defined for type " + type + ", bean names: " + names);
        }

        return (T) getBean(names[0]);
    }

    public void registerBean(String name, Object bean) throws Exception {
        Object existsBean = beansMap.get(name);
        if (existsBean != null) {
            throw new Exception("bean with name " + name + " already exist: " + bean);
        }
        beansMap.put(name, bean);
    }

    @SneakyThrows
    protected <T> void registerBean(String qualifiedName, Class<T> type) {
        String beanName = qualifiedName;
        if ("".equals(beanName)) {
            int seed = 1;
            while (true) {
                String generatedBeanName = generateBeanName(type, seed++);
                if (beansMap.get(beanName) == null) {
                    beanName = generatedBeanName;
                    break;
                }
            }
        }
        registerBean(beanName, type.newInstance());
    }

    protected <T> String generateBeanName(Class<T> type, int seed) {
        return type.getName() + "#" + String.valueOf(seed);
    }

    protected void init(String basePackage) {
        scanBeans(basePackage);
        doInject();
    }

    @SneakyThrows
    protected void doInject() {
        for (Object bean : beansMap.values()) {
            Class<?> beanClass = bean.getClass();
            for(Field field : beanClass.getDeclaredFields()) {
                Inject inject = field.getAnnotation(Inject.class);
                if (inject != null) {
                    doInject(bean, field);
                }
            }
        }
    }

    @SneakyThrows
    protected void doInject(Object bean, Field field) {
        Named named = field.getAnnotation(Named.class);
        Object injection = named != null ? getBean(named.value()) : getBean(field.getType());
        Method method = bean.getClass().getDeclaredMethod("set" + StringUtils.capitalize(field.getName()), field.getType());
        method.invoke(bean, injection);
    }

    @SneakyThrows
    protected void scanBeans(String basePackage) {
        ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
        ImmutableSet<ClassPath.ClassInfo> classInfos = classPath.getTopLevelClassesRecursive(basePackage);
        for (ClassPath.ClassInfo classInfo : classInfos) {
            Class<?> clazz = Class.forName(classInfo.getName());
            Bean beanAnnotation = clazz.getAnnotation(Bean.class);
            if (beanAnnotation != null) {
                registerBean(beanAnnotation.value(), clazz);
            }
        }
    }

    protected <T> String[] doGetBeansNameByType(Class<T> type) {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Object> entry : beansMap.entrySet()) {
            if (type.isAssignableFrom(entry.getValue().getClass())) {
                names.add(entry.getKey());
            }
        }

        return names.toArray(new String[names.size()]);
    }
}
