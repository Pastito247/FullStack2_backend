package com.fullstack2.backend.dto;

import lombok.Data;

@Data
public class CharacterInventoryUpdateRequest {
    private Long itemId;
    private Integer quantity; // cantidad a sumar (puede ser negativa para restar todo)
}
