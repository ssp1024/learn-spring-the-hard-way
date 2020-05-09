package com.ssp1024.tutorial.simplecontainer.beans;

import com.ssp1024.tutorial.simplecontainer.Eatable;
import lombok.Setter;

public class Person {
    @Setter private Eatable food;

    public void eat() {
        System.out.println("eat " + food.getName());
    }
}
