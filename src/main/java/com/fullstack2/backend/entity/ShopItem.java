package com.fullstack2.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tienda que ofrece el ítem
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    // Ítem (oficial o custom) que se vende
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    // Stock disponible
    @Column(nullable = false)
    private Integer stock;

    // Si quieres sobrescribir el precio base del ítem en esta tienda
    @Column(name = "price_override_gold")
    private Integer priceOverrideGold;
}
