package com.ssp1024.tutorial.simplecontainer;

import com.ssp1024.tutorial.simplecontainer.beans.Person;
import com.ssp1024.tutorial.simplecontainer.core.XmlBeanFactory;

public class SimpleContainerMain {
    public static void main(String[] args) {
        XmlBeanFactory beanFactory = XmlBeanFactory.fromClassPathXml("beans.xml");
        Person person = (Person) beanFactory.getBean("person");
        person.eat();
    }
}
