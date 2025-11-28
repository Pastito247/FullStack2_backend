package com.fullstack2.backend.dto;

import lombok.Data;

@Data
public class ShopTransactionRequest {

    // ShopItem = relación tienda + ítem + stock + precio
    private Long shopItemId;

    // Personaje que compra/vende
    private Long characterId;

    // Cantidad
    private int quantity;
}
