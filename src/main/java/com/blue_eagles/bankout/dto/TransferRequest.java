package com.blue_eagles.bankout.dto;
import lombok.Data;


@Data
public class TransferRequest {
    private Long accountId;
    private Double amount;
    private String recipientName; // Only for "Send Funds"
    private String recipientIban; // Only for "Send Funds"
}
