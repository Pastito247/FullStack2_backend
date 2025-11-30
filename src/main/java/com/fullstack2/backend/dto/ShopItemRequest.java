package com.fullstack2.backend.dto;

import lombok.Data;

@Data
public class ShopItemRequest {

    private Long itemId;
    private Integer stock;
    private Integer priceOverrideGold; // opcional
}
