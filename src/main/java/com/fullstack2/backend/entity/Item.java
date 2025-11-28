package com.fullstack2.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del objeto
    @Column(nullable = false)
    private String name;

    // Categoría general (arma, armadura, equipo, etc.)
    private String category;

    // Clase de armadura, si aplica
    private Integer armorClass;

    // Daño (ej: "1d8")
    private String damageDice;

    // Tipo de daño (slashing, piercing, etc.)
    private String damageType;

    // Alcance (melee / ranged)
    private String weaponRange;

    // Rango normal y largo (para armas a distancia)
    private Integer rangeNormal;
    private Integer rangeLong;

    // Propiedades en formato texto (ej: "finesse, light")
    @Column(length = 1000)
    private String properties;

    // Precio base en piezas de oro (para calcular conversiones)
    @Column(nullable = false)
    @Builder.Default
    private int basePriceGold = 0;

    // Rareza (común, poco común, etc.) – opcional, por ahora texto libre
    private String rarity;

    // Descripción libre
    @Column(length = 2000)
    private String description;

    // Fuente: OFFICIAL (API DnD) o CUSTOM
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSource source;

    // Index oficial en la API DnD5e (ejemplo: "longsword")
    private String dnd5eIndex;

    // URL de imagen opcional para el front
    private String imageUrl;

    // Usuario que creó este ítem (normalmente el DM) – puede ser null para OFFICIAL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdBy;
}
