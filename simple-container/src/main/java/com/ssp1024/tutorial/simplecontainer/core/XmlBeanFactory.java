package com.ssp1024.tutorial.simplecontainer.core;

import lombok.SneakyThrows;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

public class XmlBeanFactory {
    private Map<String, Object> beansMap = new HashMap<>();
    private Map<Class, String[]> beansNameByTypeCache = new HashMap<>();

    public static XmlBeanFactory fromClassPathXml(String path) {
        XmlBeanFactory instance = new XmlBeanFactory();
        instance.init(path);
        return instance;
    }

    public Object getBean(String name) {
        return beansMap.get(name);
    }

    public <T> void registerBean(String name, T bean) throws Exception {
        Object existBean = beansMap.get(name);
        if (existBean != null) {
            throw new Exception("bean " + name + " already exists: " + existBean);
        }

        beansMap.put(name, bean);
    }

    public <T> T getBean(Class<T> type) throws Exception {
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

    protected void init(String path) {
        Element root = parseXml(path);
        initBeans(root);
        initDI(root);
    }

    @SneakyThrows
    protected Element parseXml(String path) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);

        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        return document.getRootElement();
    }

    @SneakyThrows
    protected void initBeans(Element root) {
        for (Iterator<Element> iterator = root.elementIterator(); iterator.hasNext(); ) {
            Element element = iterator.next();
            String id = element.attribute("id").getText();
            String className = element.attribute("class").getText();

            Class<?> beanClass = Class.forName(className);
            Object object = beanClass.newInstance();
            registerBean(id, object);
        }
    }

    @SneakyThrows
    protected void initDI(Element root) {
        for (Iterator<Element> iterator = root.elementIterator(); iterator.hasNext(); ) {
            Element element = iterator.next();
            String id = element.attribute("id").getText();
            String className = element.attribute("class").getText();

            Class beanClass = Class.forName(className);
            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (Iterator<Element> propertyIt = element.elementIterator("property"); propertyIt.hasNext(); ) {
                Element propertyElement = propertyIt.next();
                String name = propertyElement.attribute("name").getText();
                String ref = propertyElement.attribute("ref").getText();
                for (PropertyDescriptor descriptor : propertyDescriptors) {
                    if (descriptor.getName().equalsIgnoreCase(name)) {
                        Method method = descriptor.getWriteMethod();
                        method.invoke(beansMap.get(id), beansMap.get(ref));
                    }
                }
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
