package com.blue_eagles.bankout.controller;

import com.blue_eagles.bankout.entity.DiscountOffer;
import com.blue_eagles.bankout.entity.UserDiscountObtained;
import com.blue_eagles.bankout.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offers")
public class OfferController {

    @Autowired
    private OfferService offerService;

    // Endpoint: /offers (GET or POST according to your doc, usually GET for viewing)
    @GetMapping
    public List<DiscountOffer> getOffers() {
        return offerService.getAllOffers();
    }

    // Endpoint: /offers/{id}/redeem
    @PostMapping("/{id}/redeem")
    public UserDiscountObtained redeemOffer(@PathVariable Long id, @RequestParam Long userId) {
        // In a real app, userId would come from the Security Context (JWT), not a param
        return offerService.redeemOffer(userId, id);
    }
}