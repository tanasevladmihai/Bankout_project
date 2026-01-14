package com.blue_eagles.bankout.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class OfferRequest {
    private Long storeId;
    private String title;
    private String description;
    private BigDecimal discountValue;
    private String discountType; // e.g., "PERCENTAGE"
    private String expiryDate; // String format yyyy-MM-dd is easier to handle from JS
}
