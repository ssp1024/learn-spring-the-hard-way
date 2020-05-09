package com.ssp1024.tutorial.scanbeans.beans;

import com.ssp1024.tutorial.scanbeans.Eatable;
import com.ssp1024.tutorial.scanbeans.core.Bean;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;

@Data
@Bean("person")
public class Person {
    @Inject
    @Named("orange")
    private Eatable food;

    public void eat() {
        System.out.println("eat " + food.getName());
    }
}
