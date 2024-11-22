package com.lukethep.domain;

import static com.lukethep.domain.ProductTestSamples.*;
import static com.lukethep.domain.UserTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lukethep.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Product.class);
        Product product1 = getProductSample1();
        Product product2 = new Product();
        assertThat(product1).isNotEqualTo(product2);

        product2.setId(product1.getId());
        assertThat(product1).isEqualTo(product2);

        product2 = getProductSample2();
        assertThat(product1).isNotEqualTo(product2);
    }

    @Test
    void ownerTest() {
        Product product = getProductRandomSampleGenerator();
        User userBack = getUserRandomSampleGenerator();

        product.setOwner(userBack);
        assertThat(product.getOwner()).isEqualTo(userBack);

        product.owner(null);
        assertThat(product.getOwner()).isNull();
    }
}
