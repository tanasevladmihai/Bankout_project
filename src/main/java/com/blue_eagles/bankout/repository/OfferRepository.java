package com.blue_eagles.bankout.repository;

import com.blue_eagles.bankout.entity.DiscountOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OfferRepository extends JpaRepository<DiscountOffer, Long> {
    // Finds all offers for a specific store
    List<DiscountOffer> findByStoreId(Long storeId);
}