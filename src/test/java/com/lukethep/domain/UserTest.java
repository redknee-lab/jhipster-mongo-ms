package com.lukethep.domain;

import static com.lukethep.domain.ProductTestSamples.*;
import static com.lukethep.domain.UserTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lukethep.web.rest.TestUtil;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(User.class);
        User user1 = getUserSample1();
        User user2 = new User();
        assertThat(user1).isNotEqualTo(user2);

        user2.setId(user1.getId());
        assertThat(user1).isEqualTo(user2);

        user2 = getUserSample2();
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void productsTest() {
        User user = getUserRandomSampleGenerator();
        Product productBack = getProductRandomSampleGenerator();

        user.addProducts(productBack);
        assertThat(user.getProducts()).containsOnly(productBack);
        assertThat(productBack.getOwner()).isEqualTo(user);

        user.removeProducts(productBack);
        assertThat(user.getProducts()).doesNotContain(productBack);
        assertThat(productBack.getOwner()).isNull();

        user.products(new HashSet<>(Set.of(productBack)));
        assertThat(user.getProducts()).containsOnly(productBack);
        assertThat(productBack.getOwner()).isEqualTo(user);

        user.setProducts(new HashSet<>());
        assertThat(user.getProducts()).doesNotContain(productBack);
        assertThat(productBack.getOwner()).isNull();
    }
}
