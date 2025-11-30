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

    // ==========================
    // Helpers de dinero (pp/gp/ep/sp/cp)
    // ==========================

    /**
     * Devuelve el total de dinero del personaje expresado en cobre (cp).
     * 1 pp = 10 gp = 1000 cp
     * 1 gp = 100 cp
     * 1 ep = 50 cp
     * 1 sp = 10 cp
     */
    public int getTotalCopper() {
        int total = 0;
        total += this.pp * 1000;
        total += this.gp * 100;
        total += this.ep * 50;
        total += this.sp * 10;
        total += this.cp;
        return total;
    }

    /**
     * Distribuye un monto total en cobre entre pp/gp/ep/sp/cp.
     * Si llega un número negativo, se normaliza a 0.
     */
    public void setFromTotalCopper(int totalCopper) {
        if (totalCopper < 0) {
            totalCopper = 0;
        }

        int pp = totalCopper / 1000;
        totalCopper %= 1000;

        int gp = totalCopper / 100;
        totalCopper %= 100;

        int ep = totalCopper / 50;
        totalCopper %= 50;

        int sp = totalCopper / 10;
        totalCopper %= 10;

        int cp = totalCopper;

        this.pp = pp;
        this.gp = gp;
        this.ep = ep;
        this.sp = sp;
        this.cp = cp;
    }
}
