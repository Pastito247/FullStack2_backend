package com.fullstack2.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopItemResponse {

    private Long id;
    private Long itemId;

    private String itemName;
    private String itemCategory;
    private String itemRarity;
    private Integer basePriceGold;

    private Integer stock;
    private Integer priceOverrideGold;
}
