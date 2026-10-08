package com.spamer.payment;

import com.spamer.domain.UserTier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayServiceTest {

    private final PayService payService = new PayService();

    @Test
    void freeTierAllowsHighRate() {
        assertDoesNotThrow(() ->
                payService.validateCampaignLimits(UserTier.FREE, 500, 2, 100));
    }

    @Test
    void rateHasNoTierCap() {
        assertEquals(Integer.MAX_VALUE, payService.maxRatePerSecond(UserTier.FREE));
        assertEquals(Integer.MAX_VALUE, payService.maxRatePerSecond(UserTier.PRO));
    }

    @Test
    void freeTierRejectsHighConcurrency() {
        assertThrows(IllegalArgumentException.class, () ->
                payService.validateCampaignLimits(UserTier.FREE, 5, 20, 100));
    }

    @Test
    void proTierAllowsHigherLimits() {
        assertDoesNotThrow(() ->
                payService.validateCampaignLimits(UserTier.PRO, 500, 20, 5000));
    }
}
