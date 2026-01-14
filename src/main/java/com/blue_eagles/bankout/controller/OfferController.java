package com.blue_eagles.bankout.controller;

import com.blue_eagles.bankout.dto.OfferRequest;
import com.blue_eagles.bankout.entity.DiscountOffer;
import com.blue_eagles.bankout.entity.Store;
import com.blue_eagles.bankout.entity.UserDiscountObtained;
import com.blue_eagles.bankout.repository.OfferRepository;
import com.blue_eagles.bankout.repository.StoreRepository;
import com.blue_eagles.bankout.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offers")
public class OfferController {

    @Autowired private OfferService offerService;
    @Autowired private OfferRepository offerRepository;
    @Autowired private StoreRepository storeRepository;

    @GetMapping
    public List<DiscountOffer> getOffers() {
        return offerService.getAllOffers();
    }

    // NEW: Get offers redeemed by the specific user
    @GetMapping("/my-redemptions")
    public List<UserDiscountObtained> getMyRedemptions(@RequestParam Long userId) {
        return offerService.getUserRedemptions(userId);
    }

    @PostMapping("/{id}/redeem")
    public UserDiscountObtained redeemOffer(@PathVariable Long id, @RequestParam Long userId) {
        return offerService.redeemOffer(userId, id);
    }

    // (Keep your existing create method here)
    @PostMapping("/create")
    public DiscountOffer createOffer(@RequestBody OfferRequest req) {
        DiscountOffer offer = new DiscountOffer();
        offer.setTitle(req.getTitle());
        offer.setDescription(req.getDescription());
        offer.setDiscountValue(req.getDiscountValue());
        offer.setDiscountType(req.getDiscountType());

        Store store = storeRepository.findById(req.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));
        offer.setStore(store);

        try {
            if (req.getExpiryDate() != null) {
                offer.setExpiryDate(java.sql.Date.valueOf(req.getExpiryDate()));
            }
        } catch (Exception e) { /* ignore */ }

        return offerRepository.save(offer);
    }
}