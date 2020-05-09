package com.ssp1024.tutorial.scanbeans;

import com.ssp1024.tutorial.scanbeans.beans.Person;
import com.ssp1024.tutorial.scanbeans.core.AnnotationBeanFactory;

public class ScanBeansMain {
    public static void main(String[] args) {
        AnnotationBeanFactory beanFactory = AnnotationBeanFactory.fromBasePackage("com.ssp1024.tutorial.scanbeans");
        beanFactory.getBean(Person.class).eat();
    }
}
