package com.lukethep.domain;

import java.util.UUID;

public class ProductTestSamples {

    public static Product getProductSample1() {
        return new Product().id("id1").name("name1");
    }

    public static Product getProductSample2() {
        return new Product().id("id2").name("name2");
    }

    public static Product getProductRandomSampleGenerator() {
        return new Product().id(UUID.randomUUID().toString()).name(UUID.randomUUID().toString());
    }
}
