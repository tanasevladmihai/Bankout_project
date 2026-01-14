package com.blue_eagles.bankout.service;

import com.blue_eagles.bankout.entity.DiscountOffer;
import com.blue_eagles.bankout.entity.UserDiscountObtained;
import com.blue_eagles.bankout.repository.OfferRepository;
import com.blue_eagles.bankout.repository.UserDiscountObtainedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OfferService {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserDiscountObtainedRepository redemptionRepository;

    public List<DiscountOffer> getAllOffers() {
        return offerRepository.findAll();
    }

    // Get all codes claimed by this specific user
    public List<UserDiscountObtained> getUserRedemptions(Long userId) {
        return redemptionRepository.findByUserId(userId);
    }

    @Transactional
    public UserDiscountObtained redeemOffer(Long userId, Long offerId) {
        // 1. Check if user already claimed this specific offer
        Optional<UserDiscountObtained> existing = redemptionRepository.findByUserIdAndOfferId(userId, offerId);
        if (existing.isPresent()) {
            return existing.get(); // Return the existing code, don't generate a new one
        }

        // 2. Validate Offer
        DiscountOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        // 3. Generate Code
        String uniqueCode = "BNK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 4. Save
        UserDiscountObtained redemption = new UserDiscountObtained();
        redemption.setUserId(userId);
        redemption.setOffer(offer);
        redemption.setUsageCode(uniqueCode);

        return redemptionRepository.save(redemption);
    }
}