package com.blue_eagles.bankout.service;

import com.blue_eagles.bankout.entity.DiscountOffer;
import com.blue_eagles.bankout.entity.User;
import com.blue_eagles.bankout.entity.UserDiscountObtained;
import com.blue_eagles.bankout.repository.OfferRepository;
import com.blue_eagles.bankout.repository.UserDiscountObtainedRepository;
import com.blue_eagles.bankout.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private UserRepository userRepository;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    public List<DiscountOffer> getAllOffers() {
        return offerRepository.findAll();
    }

    public List<UserDiscountObtained> getUserRedemptions() {
        return redemptionRepository.findByUserId(getCurrentUserId());
    }

    @Transactional
    public UserDiscountObtained redeemOffer(Long offerId) {
        Long userId = getCurrentUserId();

        Optional<UserDiscountObtained> existing = redemptionRepository.findByUserIdAndOfferId(userId, offerId);
        if (existing.isPresent()) {
            return existing.get();
        }

        DiscountOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        String uniqueCode = "BNK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        UserDiscountObtained redemption = new UserDiscountObtained();
        redemption.setUserId(userId);
        redemption.setOffer(offer);
        redemption.setUsageCode(uniqueCode);

        return redemptionRepository.save(redemption);
    }
}
