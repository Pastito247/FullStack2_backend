package com.fullstack2.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "characters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del PJ
    @Column(nullable = false)
    private String name;

    // Clase y raza de DnD
    @Column(nullable = false)
    private String dndClass;

    @Column(nullable = false)
    private String race;

    // Nivel del PJ
    @Column(nullable = false)
    private Integer level;

    // ¿Es NPC?
    @Column(nullable = false)
    private boolean npc;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // Dinero usando economía DnD
    // pp = platinum pieces, gp = gold pieces, etc.
    @Column(nullable = false)
    @Builder.Default
    private int pp = 0;

    @Column(nullable = false)
    @Builder.Default
    private int gp = 0;

    @Column(nullable = false)
    @Builder.Default
    private int ep = 0;

    @Column(nullable = false)
    @Builder.Default
    private int sp = 0;

    @Column(nullable = false)
    @Builder.Default
    private int cp = 0;

    // Campaña a la que pertenece
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Campaign campaign;

    // Jugador asignado (puede ser null si es NPC o aún no asignado)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User player;

    // Inventario del personaje
    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CharacterItem> inventory = new ArrayList<>();
}
