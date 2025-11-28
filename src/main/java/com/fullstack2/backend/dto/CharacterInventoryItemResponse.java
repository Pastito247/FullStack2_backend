package com.fullstack2.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CharacterInventoryItemResponse {

    private Long itemId;
    private String name;
    private int quantity;

    private String category;

    private String damageDice;
    private String damageType;

    private int basePriceGold;
}
