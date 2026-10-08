package com.spamer.payment;

import com.spamer.domain.UserTier;
import org.springframework.stereotype.Service;

@Service
public class PayService {

    public int maxRatePerSecond(UserTier tier) {
        return Integer.MAX_VALUE;
    }

    public int maxConcurrency(UserTier tier) {
        return tier == UserTier.PRO ? 50 : 5;
    }

    public int maxTotalRequests(UserTier tier) {
        return tier == UserTier.PRO ? 100_000 : 1_000;
    }

    public void validateCampaignLimits(UserTier tier, int rate, int concurrency, int total) {
        if (rate < 1) {
            throw new IllegalArgumentException("Rate must be at least 1 RPS");
        }
        if (concurrency > maxConcurrency(tier)) {
            throw new IllegalArgumentException("Concurrency exceeds tier limit: " + maxConcurrency(tier));
        }
        if (total > maxTotalRequests(tier)) {
            throw new IllegalArgumentException("Total requests exceed tier limit: " + maxTotalRequests(tier));
        }
    }
}
