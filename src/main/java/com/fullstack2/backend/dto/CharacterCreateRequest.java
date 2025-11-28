package com.fullstack2.backend.dto;

import lombok.Data;

@Data
public class CharacterCreateRequest {

    // Campaña a la que pertenece el personaje
    private Long campaignId;

    private String name;
    private String dndClass;
    private String race;
    private Integer level;
    private boolean npc;

    // Imagen opcional
    private String imageUrl;

    // Opcional: si el DM quiere asignar directo a un jugador por username
    private String playerUsername;

    // Dinero inicial (si vienen null, luego en el service se ponen en 0)
    private Integer pp;
    private Integer gp;
    private Integer ep;
    private Integer sp;
    private Integer cp;
}
