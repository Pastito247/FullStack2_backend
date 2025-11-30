package com.fullstack2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopItemResponse {

    // ID del registro ShopItem (ítem dentro de la tienda)
    private Long id;

    // ID del Item global
    private Long itemId;

    // Datos que el front muestra en la tabla
    private String name;
    private String category;
    private String source;      // OFFICIAL / CUSTOM
    private String rarity;

    // Precios
    private Integer basePriceGold;      // precio base del Item
    private Integer priceOverrideGold;  // override específico de la tienda
    private Integer finalPriceGold;     // precio final usado en el front

    // Stock disponible en la tienda
    private Integer stock;
}
