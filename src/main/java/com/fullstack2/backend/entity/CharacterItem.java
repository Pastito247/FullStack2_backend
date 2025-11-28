package com.fullstack2.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "character_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CharacterItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Personaje dueño del ítem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CharacterEntity character;

    // Ítem concreto (de la tabla items)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Item item;

    // Cantidad que tiene el personaje
    @Column(nullable = false)
    private int quantity;
}
