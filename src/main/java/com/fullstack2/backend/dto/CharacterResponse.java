package com.fullstack2.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CharacterResponse {

    private Long id;
    private String name;
    private String dndClass;
    private String race;
    private Integer level;
    private boolean npc;

    private String imageUrl;

    private int pp;
    private int gp;
    private int ep;
    private int sp;
    private int cp;

    private Long campaignId;
    private String campaignName;

    private String playerUsername;

    // 🔥 Nuevo: inventario del personaje
    private List<CharacterInventoryItemResponse> inventory;
}
