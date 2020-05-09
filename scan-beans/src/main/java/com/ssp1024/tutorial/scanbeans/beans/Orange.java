package com.ssp1024.tutorial.scanbeans.beans;

import com.ssp1024.tutorial.scanbeans.Eatable;
import com.ssp1024.tutorial.scanbeans.core.Bean;

@Bean("orange")
public class Orange implements Eatable {
    @Override
    public String getName() {
        return "orange";
    }
}
