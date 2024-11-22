package com.lukethep.domain;

import java.util.UUID;

public class UserTestSamples {

    public static User getUserSample1() {
        return new User().id("id1").name("name1").email("email1");
    }

    public static User getUserSample2() {
        return new User().id("id2").name("name2").email("email2");
    }

    public static User getUserRandomSampleGenerator() {
        return new User().id(UUID.randomUUID().toString()).name(UUID.randomUUID().toString()).email(UUID.randomUUID().toString());
    }
}
