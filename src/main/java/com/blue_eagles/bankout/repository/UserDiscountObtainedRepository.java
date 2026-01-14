package com.blue_eagles.bankout.repository;

import com.blue_eagles.bankout.entity.UserDiscountObtained;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserDiscountObtainedRepository extends JpaRepository<UserDiscountObtained, Long> {
    List<UserDiscountObtained> findByUserId(Long userId);
    Optional<UserDiscountObtained> findByUserIdAndOfferId(Long userId, Long offerId);
}