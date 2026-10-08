package com.spamer.outer.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhoneNormalizerTest {

    @Test
    void convertsRussianMobileToE164() {
        assertEquals("+79312894756", PhoneNormalizer.toE164("89312894756"));
    }

    @Test
    void convertsRussianMobileToNational() {
        assertEquals("89312894756", PhoneNormalizer.toNational("+79312894756"));
    }

    @Test
    void convertsRussianMobileToMobile7() {
        assertEquals("79312893745", PhoneNormalizer.toMobile7("89312893745"));
    }
}
