package com.blue_eagles.bankout.service;

import com.blue_eagles.bankout.entity.DiscountOffer;
import com.blue_eagles.bankout.entity.UserDiscountObtained;
import com.blue_eagles.bankout.repository.OfferRepository;
import com.blue_eagles.bankout.repository.UserDiscountObtainedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OfferService {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserDiscountObtainedRepository redemptionRepository;

    // Step 1: Browse Deals (Find all active offers)
    public List<DiscountOffer> getAllOffers() {
        return offerRepository.findAll();
    }

    // Step 2: Get Coupon (Redeem logic)
    @Transactional
    public UserDiscountObtained redeemOffer(Long userId, Long offerId) {
        // 1. Validate the offer exists
        DiscountOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        // 2. Create a unique code (Logic from Flow Diagram Step 6)
        // Generates a string like "BNK-SUMMER-A1B2"
        String uniqueCode = "BNK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 3. Save "User + Deal + Code" (Logic from Flow Diagram Step 7)
        UserDiscountObtained redemption = new UserDiscountObtained();
        redemption.setUserId(userId);
        redemption.setOffer(offer);
        redemption.setUsageCode(uniqueCode);

        return redemptionRepository.save(redemption);
    }
}